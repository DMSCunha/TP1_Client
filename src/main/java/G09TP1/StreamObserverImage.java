package G09TP1;
import ClientRegisterStubs.Address;
import ClienteServiceServerStub.ClientServiceGrpc;

import ClienteServiceServerStub.Id;
import ClienteServiceServerStub.Image;
import io.grpc.stub.StreamObserver;

public class StreamObserverImage implements StreamObserver<Id> {

    private int id;
    private boolean isCompleted=false;
    public boolean isCompleted(){
        return isCompleted;
    }


    public  StreamObserverImage(){

    }


    @Override
    public void onNext(Id id) {
        this.id = id.getId();

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {
        System.out.println("O id da sua imagem é: " + this.id);
        isCompleted = true;
    }
}
