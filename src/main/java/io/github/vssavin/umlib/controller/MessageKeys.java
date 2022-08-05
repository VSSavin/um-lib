package io.github.vssavin.umlib.controller;

/**
 * @author vssavin on 30.12.21
 */
public enum MessageKeys {
    USER_CREATED_SUCCESSFULLY_PATTERN("userCreatedSuccessfullyPattern"),
    USER_EXISTS_PATTERN("userExistsPattern"),
    PASSWORDS_MUST_BE_IDENTICAL_MESSAGE("passwordsMustBeIdenticalMessage"),
    USER_EDIT_ERROR_MESSAGE("userEditErrorMessage"),
    USER_EDIT_SUCCESS_MESSAGE("userEditSuccessMessage"),
    CREATE_USER_ERROR_MESSAGE("createUserErrorMessage"),
    AUTHENTICATION_REQUIRED_MESSAGE("authenticationRequiredMessage"),
    ADMIN_AUTHENTICATION_REQUIRED_MESSAGE("adminAuthenticationRequiredMessage"),
    EMAIL_EXISTS_MESSAGE("emailExistsMessage"),
    EMAIL_NOT_VALID_MESSAGE("emailNotValidMessage"),
    WRONG_PASSWORD_MESSAGE("wrongPasswordMessage"),
    REQUEST_PROCESSING_ERROR("requestProcessingError"),
    PASSWORD_SUCCESSFULLY_CHANGED_MESSAGE("passwordSuccessfullyChangedMessage"),
    USER_NOT_FOUND_MESSAGE("userNotFoundMessage"),
    CONFIRM_SUCCESS_MESSAGE("confirmSuccessMessage"),
    CONFIRM_FAILED_MESSAGE("confirmFailedMessage");

    private final String messageKey;

    MessageKeys(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
