package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class NewDirection {

    private String changeBailDirectionList;
    private String changeBailDirectionDueDateExplanation;
    private String changeBailDirectionDueDateParties;
    private String changeBailDirectionDueDateDateSent;
    private String newBailDirectionDueDate;

    private NewDirection() {
    }

    public NewDirection(
        String changeBailDirectionList,
        String changeBailDirectionDueDateExplanation,
        String changeBailDirectionDueDateParties,
        String changeBailDirectionDueDateDateSent,
        String newBailDirectionDueDate
    ) {
        this.changeBailDirectionList = requireNonNull(changeBailDirectionList);
        this.changeBailDirectionDueDateExplanation = requireNonNull(changeBailDirectionDueDateExplanation);
        this.changeBailDirectionDueDateParties = requireNonNull(changeBailDirectionDueDateParties);
        this.changeBailDirectionDueDateDateSent = requireNonNull(changeBailDirectionDueDateDateSent);
        this.newBailDirectionDueDate = requireNonNull(newBailDirectionDueDate);
    }


    public String getChangeBailDirectionList() {
        return requireNonNull(changeBailDirectionList);
    }

    public String getChangeBailDirectionDueDateExplanation() {
        return requireNonNull(changeBailDirectionDueDateExplanation);
    }

    public String getChangeBailDirectionDueDateParties() {
        return requireNonNull(changeBailDirectionDueDateParties);
    }

    public String getChangeBailDirectionDueDateDateSent() { return requireNonNull(changeBailDirectionDueDateDateSent);}

    public String getNewBailDirectionDueDate() {
        return requireNonNull(newBailDirectionDueDate);
    }

}
