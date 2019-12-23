package nl.jacbeekers;

import org.apache.log4j.Logger;

public class Logging {
    String resultCode=null;
    String resultMessage=null;
    Logger logger;

    public Logging(Logger logger) {
        setLogger(logger);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    public Logger getLogger() {
        return this.logger;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return this.resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    //
    // logging, result handling
    //
    public void logVerbose(String msg) {
        logger.trace(msg);
    }

    public void logDebug(String procName, String msg) {
        logger.debug(procName + " - " + msg);
    }

    public void logDebug(String msg) {
        logger.debug(msg);
    }

    public void logWarning(String msg) {
        logger.warn(msg);
    }

    public void logError(String resultCode, String msg) {
        setResult(resultCode, msg);
        logger.error(msg);
    }

    private void setResult(String resultCode, String msg) {
        setResultCode(resultCode);
        if (msg == null) {
            setResultMessage(Constants.getResultMessage(resultCode));
        } else {
            setResultMessage(Constants.getResultMessage(resultCode)
                    + ": " + msg);
        }
    }

    public void logFatal(String resultCode) {
        logFatal(resultCode, Constants.getResultMessage(resultCode));
    }

    public void logFatal(String resultCode, String msg) {
        setResult(resultCode, msg);
        logger.fatal(msg);
    }

    public void failSession(String resultCode) {
        failSession(resultCode, null);
    }

    public void failSession(String resultCode, String msg) {
        logError(resultCode, msg);
    }


}
