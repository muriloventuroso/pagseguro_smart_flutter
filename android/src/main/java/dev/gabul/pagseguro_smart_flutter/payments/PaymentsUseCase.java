package dev.gabul.pagseguro_smart_flutter.payments;

import java.util.Locale;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagCustomPrinterLayout;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.data.request.PlugPagBeepData;
import br.com.uol.pagseguro.plugpagservice.wrapper.exception.PlugPagException;
import dev.gabul.pagseguro_smart_flutter.core.ActionResult;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class PaymentsUseCase {

  private final PlugPag mPlugPag;

  private final int TYPE_CREDITO = 1;
  private final int TYPE_DEBITO = 2;
  private final int TYPE_VOUCHER = 3;
  private final int TYPE_PIX = 5;

  private final int INSTALLMENT_TYPE_A_VISTA = 1;
  private final int INSTALLMENT_TYPE_PARC_VENDEDOR = 2;
  private final int INSTALLMENT_TYPE_PARC_COMPRADOR = 3;
  
	public PaymentsUseCase(PlugPag plugPag) {
    mPlugPag = plugPag;
  }

  public Observable<ActionResult> doCreditPayment(
    int value,
    String userReference,
    Boolean printReceipt, 
    Boolean partialPay, 
    Boolean isCarne
  ) {
    return doPayment(
      new PlugPagPaymentData(
        TYPE_CREDITO,
        value,
        INSTALLMENT_TYPE_A_VISTA,
        1,
        userReference,
        printReceipt,
        partialPay,
        isCarne
      )
    );
  }

  public Observable<ActionResult> doCreditPaymentParc(
    int value,
    int type,
    int parc,
    String userReference,
    Boolean printReceipt, 
    Boolean partialPay, 
    Boolean isCarne
  ) {
    return doPayment(
      new PlugPagPaymentData(
        TYPE_CREDITO,
        value,
        type,
        parc,
        userReference,
        printReceipt,
        partialPay,
        isCarne
      )
    );
  }

  public Observable<ActionResult> doDebitPayment(
    int value,
    String userReference,
    Boolean printReceipt, 
    Boolean partialPay, 
    Boolean isCarne
  ) {
    return doPayment(
      new PlugPagPaymentData(
        TYPE_DEBITO,
        value,
        INSTALLMENT_TYPE_A_VISTA,
        1,
        userReference,
        printReceipt,
        partialPay,
        isCarne
      )
    );
  }

  public Observable<ActionResult> doVoucherPayment(
    int value,
    String userReference,
    Boolean printReceipt, 
    Boolean partialPay, 
    Boolean isCarne
  ) {
    return doPayment(
      new PlugPagPaymentData(
        TYPE_VOUCHER,
        value,
        INSTALLMENT_TYPE_A_VISTA,
        1,
        userReference,
        printReceipt,
        partialPay,
        isCarne
      )
    );
  }

  public Observable<ActionResult> doPixPayment(
    int value,
    String userReference,
    Boolean printReceipt, 
    Boolean partialPay, 
    Boolean isCarne
  ) {
    return doPayment(
      new PlugPagPaymentData(
        TYPE_PIX,
        value,
        INSTALLMENT_TYPE_A_VISTA,
        1,
        userReference,
        printReceipt,
        partialPay,
        isCarne
      )
    );
  }

  public Observable<ActionResult> doStartPayment(
    int type,
    int amount,
    int installmentType,
    int installments,    
    String userReference,
    Boolean printReceipt, 
    Boolean partialPay, 
    Boolean isCarne
  ) {
    return doPayment(
      new PlugPagPaymentData(
        type,
        amount,
        installmentType,
        installments,
        userReference,
        printReceipt,
        partialPay,
        isCarne
      )
    );
  }  

  private Observable<ActionResult> doPayment(
    final PlugPagPaymentData paymentData
  ) {
    return Observable.create(emitter -> {
      ActionResult result = new ActionResult();
      setListener(emitter, result);
      PlugPagTransactionResult plugPagTransactionResult = mPlugPag.doPayment(
        paymentData
      );
      sendResponse(emitter, plugPagTransactionResult, result);
    });
  }
  private void sendResponse(
    ObservableEmitter<ActionResult> emitter,
    PlugPagTransactionResult plugPagTransactionResult,
    ActionResult result
  ) {
    if (plugPagTransactionResult.getResult() != 0) {
      emitter.onError(
        new PlugPagException(
          plugPagTransactionResult.getMessage(),
          plugPagTransactionResult.getErrorCode()
        )
      );
    } else {
      result.setTransactionCode(plugPagTransactionResult.getTransactionCode());
      result.setTransactionId(plugPagTransactionResult.getTransactionId());
      result.buildResponse(plugPagTransactionResult);
      emitter.onNext(result);
    }
    emitter.onComplete();
  }

  private void sendResponse(ObservableEmitter<ActionResult> emitter, PlugPagPrintResult printResult,
                              ActionResult result) {

        if (printResult.getResult() != 0) {
            result.setResult(printResult.getResult());
        }
        emitter.onComplete();
    }

  private void setListener(
    ObservableEmitter<ActionResult> emitter,
    ActionResult result
  ) {
    mPlugPag.setEventListener(plugPagEventData -> {
      result.setEventCode(plugPagEventData.getEventCode());
      result.setMessage(plugPagEventData.getCustomMessage());
      emitter.onNext(result);
    });
  }
  public Completable abort() {
    return Completable.create(emitter -> mPlugPag.abort());
  }
  public Observable<Boolean> isAuthenticated() {
    return Observable.create(emitter -> {
      emitter.onNext(mPlugPag.isAuthenticated());
      emitter.onComplete();
    });
  }
  public Observable<ActionResult> initializeAndActivatePinpad(
    String activationCode
  ) {
    return Observable.create(emitter -> {
      ActionResult actionResult = new ActionResult();
      mPlugPag.setEventListener(plugPagEventData -> {
        actionResult.setEventCode(plugPagEventData.getEventCode());
        actionResult.setMessage(plugPagEventData.getCustomMessage());
        emitter.onNext(actionResult);
      });
      PlugPagInitializationResult result = mPlugPag.initializeAndActivatePinpad(
        new PlugPagActivationData(activationCode)
      );
      if (result.getResult() == PlugPag.RET_OK) {
        emitter.onNext(new ActionResult());
      } else {
        emitter.onError(new RuntimeException(result.getErrorMessage()));
      }
      emitter.onComplete();
    });
  }
  public Observable<ActionResult> doRefund(
    String transactionCode,
    String transactionId
  ) {
    return Observable.create(emitter -> {
      ActionResult actionResult = new ActionResult();
      setListener(emitter, actionResult);
      PlugPagTransactionResult result = mPlugPag.voidPayment(
        new PlugPagVoidData(transactionCode, transactionId, true)
      );
      sendResponse(emitter, result, actionResult);
    });
  }
  public Observable<ActionResult> getLastTransaction() {
    return Observable.create(emitter -> {
      ActionResult actionResult = new ActionResult();
      PlugPagTransactionResult result = mPlugPag.getLastApprovedTransaction();
      sendResponse(emitter, result, actionResult);
    });
  }

  public Observable<ActionResult> printStablishmentReceipt() {
      return Observable.create(emitter -> {
          ActionResult actionResult = new ActionResult();
          setPrintListener(emitter, actionResult);
          PlugPagPrintResult result = mPlugPag.reprintStablishmentReceipt();
          sendResponse(emitter, result, actionResult);
      });
  }

  public Observable<ActionResult> printCustomerReceipt() {
      return Observable.create(emitter -> {
          ActionResult actionResult = new ActionResult();
          setPrintListener(emitter, actionResult);
          PlugPagPrintResult result = mPlugPag.reprintCustomerReceipt();
          sendResponse(emitter, result, actionResult);
      });
  }

  private void setPrintListener(ObservableEmitter<ActionResult> emitter, ActionResult result) {
      mPlugPag.setPrinterListener(new PlugPagPrinterListener() {
          @Override
          public void onError(PlugPagPrintResult printResult) {
              result.setResult(printResult.getResult());
              result.setMessage(String.format("Error %s %s", printResult.getErrorCode(), printResult.getMessage()));
              result.setErrorCode(printResult.getErrorCode());
              emitter.onNext(result);
          }

          @Override
          public void onSuccess(PlugPagPrintResult printResult) {
              result.setResult(printResult.getResult());
              result.setMessage(String.format(Locale.getDefault(),"Print OK: Steps [%d]", printResult.getSteps()));
              result.setErrorCode(printResult.getErrorCode());
              emitter.onNext(result);
          }
      });
  }

  public void rebootDevice() {
    try {
      mPlugPag.reboot();
    }catch (Exception e){
    }
  }

  public void beep() {
    try {
      PlugPagBeepData beepData = new PlugPagBeepData(
              PlugPagBeepData.FREQUENCE_LEVEL_1, 200);
      mPlugPag.beep(beepData);
    }catch (Exception e){
    }
  }
}