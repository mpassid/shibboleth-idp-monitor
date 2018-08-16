CREATE TABLE mpass_monitoring_result (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    sourceId VARCHAR(20) NOT NULL,
    startTime BIGINT NOT NULL,
    endTime BIGINT NOT NULL
	);
CREATE TABLE mpass_monitoring_step_result (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    resultId BIGINT NOT NULL,
    phaseId TINYINT NOT NULL,
    errorMessage VARCHAR(200),
    startTime BIGINT NOT NULL,
    endTime BIGINT NOT NULL
    );