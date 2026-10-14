package agora.entity.user;

import agora.exceptions.UnknownUserTypeException;

public enum UserType {

    PERSON(0),
    ORGANIZATION(1);

    private final Integer intValue;

    UserType(Integer intValue) {
        this.intValue = intValue;
    }

    public static UserType getUserType(Integer intValue) throws UnknownUserTypeException {
        for (UserType userType : UserType.values()) {
            if (userType.intValue.equals(intValue)) {
                return userType;
            }
        }
        throw new UnknownUserTypeException("Не найден тип пользователя по значению: " + intValue);
    }
}
