package pt.ulisboa.tecnico.sec.services.dto;

public class ErrorMessageResponse {
    String errorName;
    String description;

    public ErrorMessageResponse() {}

    public ErrorMessageResponse(String errorName, String errorDescription) {
        this.errorName = errorName;
        this.description = errorDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    @Override
    public String toString() {
        return "ErrorMessageResponse{" +
                "errorName='" + errorName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
