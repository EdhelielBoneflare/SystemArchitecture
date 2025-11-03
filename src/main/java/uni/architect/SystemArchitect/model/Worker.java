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
    private double totalWorkTime = 0;

    private double getNewCompletionTime() {
        return rand.nextExponential() * maxProcessingTime;
    }

    public void startProcessing(double curTime, Request request) {
        isBusy = true;
        double workTime = getNewCompletionTime();
        completionTime = curTime + workTime;
        curRequest = request;
        totalWorkTime += workTime;
    }

    public void stopProcessing(double curTime) {
        curRequest = null;
        isBusy = false;
    }
}
