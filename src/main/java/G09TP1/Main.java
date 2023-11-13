package G09TP1;

import ClienteServiceServerStub.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import registerclientstub.Address;
import registerclientstub.*;
import registerclientstub.Void;

import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.file.Paths;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static String svcIP = "localhost";
    //private static String svcIP = "35.246.73.129";
    private static int svcPort = 8500;
    private static ManagedChannel channel;
    private static ManagedChannel channel2;

    private static Address registerAdress;

    private static RegisterClientGrpc.RegisterClientBlockingStub blockingStub;
    private static ClientServiceGrpc.ClientServiceStub noBlockStub;
    private static ClientServiceGrpc.ClientServiceBlockingStub blockingStubStatus;
    private static ClientServiceGrpc.ClientServiceStub noBlockingGetImage;


    private static int contador = 0;

    private static  String imagePath;
    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }


            //get register ip
            Scanner ip = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter resgister IP: ");
            String resgisterip = ip.nextLine();  // Read user input

            //get register port
            Scanner port = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter register port: ");
            String registerport = port.nextLine();  // Read user input
            int registerportint = Integer.parseInt(registerport);

            //cria chanel com o register
            channel =ManagedChannelBuilder.forAddress(resgisterip, registerportint).usePlaintext().build();
            //channel =ManagedChannelBuilder.forAddress("34.175.157.230", 8500).usePlaintext().build();

            blockingStub = RegisterClientGrpc.newBlockingStub(channel);



            //cria o channel com service
            boolean flag = false;
            while (!flag) {
                try {

                    //get IP and PORT for the service
                    registerAdress = blockingStub.getIP(Void.newBuilder().build());

                    System.out.println("Conectado ao serviço:\nIP: " + registerAdress.getIp() + " PORT: " + registerAdress.getPort());

                    channel2 = ManagedChannelBuilder.forAddress(registerAdress.getIp(),
                            registerAdress.getPort()).usePlaintext().build();

                    noBlockStub = ClientServiceGrpc.newStub(channel2);


                    System.out.println("chanel 2 criado");
                    flag = true;
                } catch (Exception e) {
                   e.printStackTrace();
                    Scanner again = new Scanner(System.in);
                    System.out.println("Ligção com o serviço falhada, quer tentar de novo S/N");
                    String tryagain = again.nextLine();
                    if(tryagain.equals("N") || tryagain.equals("n") ){
                        System.exit(0);
                    }
                    else {
                        blockingStub = RegisterClientGrpc.newBlockingStub(channel);
                        blockingStub.errorIP(registerAdress);
                    }


                }
            }





            while(true){
                switch (Menu()) {
                    case 1: //enviar imagem

                        //get path of image
                        Scanner path = new Scanner(System.in);  // Create a Scanner object
                        System.out.println("Enter image path:  ");
                        imagePath = path.nextLine();  // Read user input
                        Path filePath = Paths.get(imagePath);
                        //get image
                        byte[] imageData = Files.readAllBytes(filePath);
                        //divide image by 4
                        List<byte[]> byteList= divideArray(imageData, 4);
                        
                        //transform byte list in byteString
                        ByteString byteSeq0 = ByteString.copyFrom(byteList.get(0));
                        ByteString byteSeq1 = ByteString.copyFrom(byteList.get(1));
                        ByteString byteSeq2 = ByteString.copyFrom(byteList.get(2));
                        ByteString byteSeq3 = ByteString.copyFrom(byteList.get(3));

                        //get mark of image
                        Scanner text = new Scanner(System.in);  // Create a Scanner object
                        System.out.println("Enter text to mark image:  ");
                        String imageText = text.nextLine();  // Read user input

                        //get image name
                        Scanner text1 = new Scanner(System.in);  // Create a Scanner object
                        System.out.println("Enter image name:  ");
                        String imageName = text1.nextLine();  // Read user input


                        StreamObserverImage id = new StreamObserverImage();
                        StreamObserver<Image> streanmimage = noBlockStub.sendImage(id);

                        streanmimage.onNext(Image.newBuilder().setImageBytes(byteSeq0).setKeywords(imageText).setName(imageName).build());
                        streanmimage.onNext(Image.newBuilder().setImageBytes(byteSeq1).setKeywords(imageText).setName(imageName).build());
                        streanmimage.onNext(Image.newBuilder().setImageBytes(byteSeq2).setKeywords(imageText).setName(imageName).build());
                        streanmimage.onNext(Image.newBuilder().setImageBytes(byteSeq3).setKeywords(imageText).setName(imageName).build());
                        streanmimage.onCompleted();

                        System.out.println("Stream completo");
                        break;

                    case 2: //Verificar id da imagem




                        Scanner idScanner = new Scanner(System.in);  // Create a Scanner object
                        System.out.println("Image ID: ");
                        String idImage = idScanner.nextLine();  // Read user input

                        Id idStatus = Id.newBuilder().setId(Integer.parseInt(idImage)).build();

                        blockingStubStatus = ClientServiceGrpc.newBlockingStub(channel2);

                        System.out.println(blockingStubStatus.isDone(idStatus).getStatus());
                        break;

                    case 3: // get image

                        Scanner id_getImage = new Scanner(System.in);  // Create a Scanner object
                        System.out.println("Image ID: ");
                        String idG= id_getImage.nextLine();
                        Id idGetImage = Id.newBuilder().setId(Integer.parseInt(idG)).build();


                        noBlockingGetImage = ClientServiceGrpc.newStub(channel2);

                        List<ByteString>[] image = new List[4];


                        StreamObserver<MarkImage> getMImage = new StreamObserver<MarkImage>(){


                            @Override
                            public void onNext(MarkImage markImage) {
                                image[contador].add(markImage.getImageBytes());
                                contador++;
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        };

                        noBlockingGetImage.getImage(idGetImage, getMImage);

                        //concatenate the ByteString array in a single ByteString
                        ByteString finalImageBString = ByteString.empty();
                        for (List<ByteString> byteString : image) {
                            finalImageBString = finalImageBString.concat(ByteString.copyFrom(byteString));
                        }
                        //transform ByteString in byte[]
                        byte[] finalImage = finalImageBString.toByteArray();

                        int lastPeriodIndex = imagePath.lastIndexOf('.');

                        String filePathFinalImage = imagePath.substring(0, lastPeriodIndex) + "mark" + imagePath.substring(lastPeriodIndex);


                        // Write the finalImage byteArray to a .jpg file
                        try (FileOutputStream fos = new FileOutputStream(filePathFinalImage)) {
                            fos.write(finalImage);

                            System.out.println("File saved successfully as " + filePathFinalImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }



                        break;

                    case 99:
                        System.exit(0);
                        break;

                }
            }




        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Case 1 - Enviar imagem");
            System.out.println(" 2 - Case 2 - Verificar se imagem esta pronta");
            System.out.println(" 2 - Case 3 - Descarregar imagem com tag");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 4) || op == 99));
        return op;
    }


    public static List<byte[]> divideArray(byte[] source, int chunksize) {

        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }

        return result;
    }

}