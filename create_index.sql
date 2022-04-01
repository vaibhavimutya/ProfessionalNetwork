CREATE UNIQUE INDEX usr_index on USR USING BTREE (userID);

CREATE UNIQUE INDEX Work_index on Work_Ex USING BTREE (userId,company,role,startDate);

CREATE UNIQUE INDEX Edu_index on Edu_det USING BTREE (userId,major, degree);

CREATE UNIQUE INDEX Message_index on Message USING BTREE (msgId);

CREATE UNIQUE INDEX Conn_index on Connection USING BTREE (userId,connectionId);
                                                                                