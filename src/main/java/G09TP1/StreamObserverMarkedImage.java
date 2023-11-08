package G09TP1;

import ClienteServiceServerStub.Id;
import ClienteServiceServerStub.MarkImage;
import io.grpc.stub.StreamObserver;

public class StreamObserverMarkedImage implements StreamObserver<MarkImage> {

    private MarkImage markImage;
    private boolean isCompleted=false;
    public boolean isCompleted(){
        return isCompleted;
    }


    public  StreamObserverMarkedImage(){}



    @Override
    public void onNext(MarkImage markImage) {
        this.markImage.getImageBytes();
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onCompleted() {

        isCompleted = true;
    }
}

