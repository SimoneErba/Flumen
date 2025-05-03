package com.livedatatrail.backend.entities;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HasPosition {
    private String id;
    private String itemId;
    private String positionId;
    private Instant eventTime;
    private HasPositionStatus status;
    private List<Double> pastProgresses;

    public void stop(double speed) {
        if (!this.status.equals(HasPositionStatus.STOPPED)) {
            double timeMoving = (double) (Instant.now().getEpochSecond() - this.eventTime.getEpochSecond());
            double newProgress = timeMoving * speed;
            pastProgresses.add(newProgress);

            this.status = HasPositionStatus.STOPPED;
            this.eventTime = Instant.now();
        }
    }

    public void resume() {
        if (this.status.equals(HasPositionStatus.STOPPED)) {
            this.eventTime = Instant.now();
            this.status = HasPositionStatus.MOVING;
        }
    }
}