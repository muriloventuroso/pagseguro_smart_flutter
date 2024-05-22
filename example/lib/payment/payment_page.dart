import 'dart:io';
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/material.dart';

import 'package:flutter_masked_text2/flutter_masked_text2.dart';

import 'package:pagseguro_smart_flutter/pagseguro_smart_flutter.dart';
import 'package:path_provider/path_provider.dart';

import 'payment_controller.dart';

class PaymentPage extends StatefulWidget {
  const PaymentPage({Key? key}) : super(key: key);

  @override
  _PaymentPageState createState() => _PaymentPageState();
}

class _PaymentPageState extends State<PaymentPage> {
  final PaymentController controller = PaymentController();

  double? saleValue;
  MoneyMaskedTextController moneyController =
      MoneyMaskedTextController(leftSymbol: "R\$ ", decimalSeparator: ",");

  @override
  void initState() {
    //Inicializar a classe handle para escutar os métodos e retornos da pagseguro
    PagseguroSmart.instance().initPayment(controller);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        children: <Widget>[
          const SizedBox(
            height: 20,
          ),
          TextField(
            onChanged: (value) => setState(() {
              controller.setSaleValue(moneyController.numberValue);
            }),
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(hintText: "Digite o valor"),
            controller: moneyController,
          ),
          const SizedBox(
            height: 20,
          ),
          const Text(
            "Selecione o método de pagamento",
            style: TextStyle(fontSize: 16),
          ),
          const SizedBox(
            height: 20,
          ),
          Wrap(
            spacing: 10.0,
            children: <Widget>[
              ElevatedButton(
                child: const Text("Débito"),
                onPressed: controller.enable
                    ? () {
                        FocusScope.of(context).unfocus();
                        setState(() {
                          controller.clickPayment = true;
                        });
                        //Chamar o método de pagamento para transação no débito
                        PagseguroSmart.instance()
                            .payment
                            .debitPayment(controller.saleValue);
                      }
                    : null,
              ),
              ElevatedButton(
                child: const Text("Crédito"),
                onPressed: controller.enable
                    ? () {
                        FocusScope.of(context).unfocus();
                        setState(() {
                          controller.clickPayment = true;
                        });
                        //Chamar o método de pagamento para transação no crédito a vista
                        PagseguroSmart.instance()
                            .payment
                            .creditPayment(controller.saleValue);
                      }
                    : null,
              ),
              ElevatedButton(
                child: const Text("Crédito Parc- 2"),
                onPressed: controller.enable
                    ? () {
                        FocusScope.of(context).unfocus();
                        setState(() {
                          controller.clickPayment = true;
                        });
                        //Chamar o método de pagamento para transação no crédito parcelado em 2x
                        PagseguroSmart.instance()
                            .payment
                            .creditPaymentParc(controller.saleValue, 2);
                      }
                    : null,
              ),
              ElevatedButton(
                child: const Text("Voucher"),
                onPressed: controller.enable
                    ? () {
                        FocusScope.of(context).unfocus();
                        setState(() {
                          controller.clickPayment = true;
                        });
                        //Chamar o método de pagamento para transação no voucher
                        PagseguroSmart.instance()
                            .payment
                            .voucherPayment(controller.saleValue);
                      }
                    : null,
              ),
              ElevatedButton(
                child: const Text("PIX"),
                onPressed: controller.enable
                    ? () {
                        FocusScope.of(context).unfocus();
                        setState(() {
                          controller.clickPayment = true;
                        });
                        //Chamar o método de pagamento para transação no pix
                        PagseguroSmart.instance()
                            .payment
                            .pixPayment(controller.saleValue);
                      }
                    : null,
              ),
              ElevatedButton(
                child: const Text("ATIVAR PINPAD"),
                onPressed: () async {
                  FocusScope.of(context).unfocus();
                  setState(() {
                    controller.clickPayment = true;
                  });
                  //Chamar o método para ativar o terminal (pinpad)
                  PagseguroSmart.instance().payment.activePinpad('');
                },
              ),
            ],
          ),
          const SizedBox(
            height: 20,
          ),
          ElevatedButton(
            onPressed: controller.clickPayment
                ? () {
                    controller.setSaleValue(0.0);
                    //Chamar o método para abortar uma transação em andamento (processamento)
                    PagseguroSmart.instance().payment.abortTransaction();
                  }
                : null,
            child: const Text("Cancelar Operação"),
          ),
          const SizedBox(
            height: 20,
          ),
          ElevatedButton(
            onPressed: () {
              Future.delayed(const Duration(seconds: 3))
                  .then((value) => setState(() {}));
              //Chamar o método para retornar a última transação realizada
              PagseguroSmart.instance().payment.lastTransaction();
            },
            child: const Text("Ultima transação"),
          ),
          const SizedBox(
            height: 20,
          ),
          if (controller.enableRefund)
            ElevatedButton(
              onPressed: () {
                //Chamar o método para estornar uma transação
                PagseguroSmart.instance().payment.refund(
                    transactionCode: controller.transactionCode,
                    transactionId: controller.transactionId);
              },
              child: const Text("Estornar transação"),
            ),
          const SizedBox(
            height: 20,
          ),
          ElevatedButton(
            onPressed: () async {
              //Chamar o método para retornar a última transação realizada
              // var imgBytes = await getCanvasImage("Teste impressão");
              // var dir = await getExternalStorageDirectory();
              // var pathImg = "${dir!.path}/test.png";
              // var file = File(pathImg);
              // file.writeAsBytesSync(imgBytes!);
              // print(pathImg);
              PagseguroSmart.instance()
                  .payment
                  .printerfromFile("/storage/emulated/0/Download/teste.jpg");
            },
            child: const Text("Imprimir"),
          ),
        ],
      ),
    );
  }

  Future<Uint8List?> getCanvasImage(String text) async {
    final recorder = PictureRecorder();
    var newCanvas = Canvas(recorder);

    var builder = ParagraphBuilder(ParagraphStyle(fontStyle: FontStyle.normal));
    builder.addText(text);
    Paragraph paragraph = builder.build();
    paragraph.layout(const ParagraphConstraints(width: 80));
    newCanvas.drawParagraph(paragraph, Offset.zero);

    final picture = recorder.endRecording();
    var res = await picture.toImage(100, 100);
    ByteData? data = await res.toByteData(format: ImageByteFormat.png);

    if (data != null) {
      return Uint8List.view(data.buffer);
    }
    return null;
  }
}
