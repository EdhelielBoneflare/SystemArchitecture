package uni.architect.SystemArchitect.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Generator {
    private int number;
    private double genInterval;
    private int genCount;
    private double nextGenTime;

    public Generator(int number, double genInterval, int genCount) {
        this.number = number;
        this.genInterval = genInterval;
        this.genCount = genCount;
        this.nextGenTime = genInterval;
    }

    public Request generateRequest(double curTime) {
        nextGenTime = curTime + genInterval;
        genCount++;
        return new Request(number + "." + (genCount), number);
    }
}
