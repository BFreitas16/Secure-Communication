package pt.ulisboa.tecnico.sec.services.dto;

import java.util.List;

public class SpecialUserResponseDTO {

    public SpecialUserResponseDTO() {}

    private List<String> users;

    public void setUsers(List<String> witnesses) {
        this.users = witnesses;
    }

    public List<String> getUsers() {
        return users;
    }
}
