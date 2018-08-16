CREATE TABLE mpass_monitoring_result (
    id BIGINT AUTO_INCREMENT NOT NULL,
    sourceId VARCHAR(20) NOT NULL,
    startTime BIGINT NOT NULL,
    endTime BIGINT NOT NULL,
    PRIMARY KEY (id)
	);
CREATE TABLE mpass_monitoring_step_result (
    id BIGINT AUTO_INCREMENT NOT NULL,
    resultId BIGINT NOT NULL,
    phaseId TINYINT NOT NULL,
    errorMessage VARCHAR(200),
    startTime BIGINT NOT NULL,
    endTime BIGINT NOT NULL,
    PRIMARY KEY (id)
    );