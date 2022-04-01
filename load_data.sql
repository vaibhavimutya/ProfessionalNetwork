COPY USR(
        userId,
        password,
        email,
        name,
        dateOfBirth
)

FROM 'USR.csv'
DELIMITER ',' CSV HEADER;

COPY Work_Ex(
        userID,
        company,
        role,
        location,
        startDate,
        endDate
)
FROM 'Work_Ex.csv'
DELIMITER ',' CSV HEADER;

COPY Edu_det(
        userID,
        instituitionName,
        major,
        degree,
        startDate,
        endDate
)
FROM 'Edu_det.csv'
DELIMITER ',' CSV HEADER;

COPY Message(
        msgId,
        senderId,
        receiverId,
        contents,
        sendTime,
        deleteStatus,
        status)
FROM 'Message.csv'
DELIMITER ',' CSV HEADER;

COPY Connection(
        userId,
        connectionId,
        status)
                                                                                                        FROM 'Connection.csv'
DELIMITER ',' CSV HEADER
