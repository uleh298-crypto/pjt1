package com.ssafy.ssabre.global.error;

import lombok.Getter;

@Getter
public enum GlobalErrorCode implements ErrorCode {

    // Common
    INVALID_INPUT("C001", "Invalid Input Value", 400),
    METHOD_NOT_ALLOWED("C002", "Method Not Allowed", 405),
    ENTITY_NOT_FOUND("C003", "Entity Not Found", 404),
    INTERNAL_SERVER_ERROR("C004", "Server Error", 500),
    INVALID_TYPE_VALUE("C005", "Invalid Type Value", 400),
    ACCESS_DENIED("C006", "Access is Denied", 403),
    DUPLICATE_RESOURCE("C007", "Duplicate Resource", 409),
    CENSORSHIP_FAILED("C008", "Content contains inappropriate text", 400),

    // Member
    MEMBER_NOT_FOUND("M001", "Member not found", 404),

    // Study/Team
    STUDY_NOT_FOUND("S001", "Study not found", 404),
    TEAM_NOT_FOUND("T001", "Team not found", 404),
    CAMPUS_NOT_FOUND("CA001", "Campus not found", 404),
    PORTFOLIO_NOT_FOUND("P001", "Portfolio not found", 404),
    APPLICATION_NOT_FOUND("A001", "Application not found", 404),

    // Authorization
    UNAUTHORIZED_ACTION("AU001", "Unauthorized action", 403),
    NOT_GROUP_LEADER("AU002", "Only group leader can perform this action", 403),
    INVALID_PORTFOLIO_OWNER("AU003", "You do not own this portfolio", 403),
    NOT_MEMBER("AU004", "Only group members can perform this action", 403),

    // Business Logic
    ALREADY_MEMBER("B001", "Already a member of this group", 409),
    CAPACITY_EXCEEDED("B002", "Group capacity has been reached", 400),
    INVALID_APPLICATION_STATUS("B003", "Invalid application status transition", 400),
    GROUP_NOT_OPEN("B004", "Group is not open for applications", 400),
    DUPLICATE_APPLICATION("B005", "Already applied to this group", 409),
    INVALID_DATE_RANGE("B006", "Start date must be before end date", 400),
    CANNOT_KICK_SELF("B007", "Cannot kick yourself from the group", 400),
    CANNOT_KICK_LEADER("B008", "Cannot kick the group leader", 400),
    MEMBER_NOT_IN_TEAM("B009", "Member is not in this team", 404),
    MEMBER_NOT_IN_STUDY("B010", "Member is not in this study", 404),
    LEADER_CANNOT_LEAVE("B011", "Leader cannot leave the group. Please transfer leadership first.", 400),
    MEMBER_IN_GROUP("B012", "Cannot withdraw while in an active team or study. Please leave all groups first.", 400);

    private final String code;
    private final String message;
    private final int status;

    GlobalErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
