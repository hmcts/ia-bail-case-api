package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import org.w3c.dom.stylesheets.LinkStyle;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

import java.util.List;
>>>>>>> 1c61fd5 (Backend for directions screen)
=======
>>>>>>> 3c3779b (RIA-5556 Tweak Direction object)

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class Direction {

    private String sendDirectionDescription;
    private String sendDirectionList;
    private String dateOfCompliance;
    private String dateSent;

    private Direction() {
    }

    public Direction(
        String sendDirectionDescription,
        String sendDirectionList,
        String dateOfCompliance,
        String dateSent
    ) {
        this.sendDirectionDescription = requireNonNull(sendDirectionDescription);
        this.sendDirectionList = requireNonNull(sendDirectionList);
        this.dateOfCompliance = requireNonNull(dateOfCompliance);
        this.dateSent = requireNonNull(dateSent);
    }


    public String getSendDirectionDescription() {
        return requireNonNull(sendDirectionDescription);
    }

    public String getSendDirectionList() {
        return requireNonNull(sendDirectionList);
    }

    public String getDateOfCompliance() {
        return requireNonNull(dateOfCompliance);
    }

    public String getDateSent() {
        return requireNonNull(dateSent);
    }


}
