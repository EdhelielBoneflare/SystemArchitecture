package uni.architect.SystemArchitect.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
@AllArgsConstructor
public class Worker {
    private static Random rand = new Random();

    private int number;
    private boolean isBusy;
    private double completionTime;
    private double maxProcessingTime;
    private Request curRequest;

    private double getNewCompletionTime(double curTime) {
        return curTime + rand.nextExponential() * maxProcessingTime;
    }

    public void startProcessing(double curTime, Request request) {
        isBusy = true;
        completionTime = getNewCompletionTime(curTime);
        curRequest = request;
    }

    public void stopProcessing(double curTime) {
        curRequest = null;
        isBusy = false;
    }
}
