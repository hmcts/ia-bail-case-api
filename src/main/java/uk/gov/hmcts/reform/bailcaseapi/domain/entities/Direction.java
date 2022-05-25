package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.stylesheets.LinkStyle;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

import java.util.List;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class Direction {

    private String directionDescription;
    private String directionList;
    private String dateOfCompliance;
    private String user;
    private String dateAdded;


    private Direction() {
    }

    public Direction(
        String directionDescription,
        String directionList,
        String dateOfCompliance,
        String user,
        String dateAdded
    ) {
        this.directionDescription = requireNonNull(directionDescription);
        this.directionList = requireNonNull(directionList);
        this.dateOfCompliance = requireNonNull(dateOfCompliance);
        this.user = requireNonNull(user);
        this.dateAdded = requireNonNull(dateAdded);
    }


    public String getDirectionDescription() {
        return requireNonNull(directionDescription);
    }

    public String getDirectionList() {
        return requireNonNull(directionList);
    }

    public String getDateOfCompliance() {
        return requireNonNull(dateOfCompliance);
    }

    public String getUser() {
        return requireNonNull(user);
    }

    public String getDateAdded() {
        return requireNonNull(dateAdded);
    }


}
