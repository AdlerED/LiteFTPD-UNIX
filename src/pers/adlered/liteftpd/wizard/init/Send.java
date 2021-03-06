package pers.adlered.liteftpd.wizard.init;

import pers.adlered.liteftpd.analyze.PrivateVariable;
import pers.adlered.liteftpd.user.status.bind.IPAddressBind;
import pers.adlered.liteftpd.dict.Dict;
import pers.adlered.liteftpd.logger.enums.Levels;
import pers.adlered.liteftpd.logger.Logger;
import pers.adlered.liteftpd.logger.enums.Types;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

/**
 * <h3>LiteFTPD-UNIX</h3>
 * <p>Send method can be called by other threads in the same thread pool, and send a message to client.</p>
 *
 * @author : https://github.com/AdlerED
 * @date : 2019-09-19 09:21
 **/
public class Send {
    private OutputStream outputStream = null;

    private PauseListen pauseListen = null;
    private PrivateVariable privateVariable = null;
    private IPAddressBind ipAddressBind = null;

    public Send(OutputStream outputStream, PauseListen pauseListen, PrivateVariable privateVariable, IPAddressBind ipAddressBind) {
        this.outputStream = outputStream;
        this.pauseListen = pauseListen;
        this.privateVariable = privateVariable;
        this.ipAddressBind = ipAddressBind;
        send(Dict.connectedMessage(ipAddressBind.getIPADD()));
    }

    public boolean send(String message) {
        if (!privateVariable.isInterrupted()) {
            Logger.log(Types.SEND, Levels.DEBUG, "Encode is: " + privateVariable.getEncode());
            try {
                Logger.log(Types.SEND, Levels.INFO, ipAddressBind.getIPADD() + " <== [ " + privateVariable.encode + " ] " + ipAddressBind.getSRVIPADD() + ": " + message.replaceAll("\r|\n", ""));
                pauseListen.resetTimeout();
                // WELCOME MESSAGE
                try {
                    outputStream.write(message.getBytes(privateVariable.encode));
                } catch (SocketException SE) {
                    Logger.log(Types.SYS, Levels.ERROR, "Client " + ipAddressBind.getIPADD() + " socket write failed. This fake client may running port scan (such as \"nmap\"), please attention.");
                    privateVariable.reason = "Anti-scanner?";
                    privateVariable.setInterrupted(true);
                }
                outputStream.flush();
                return true;
            } catch (IOException IOE) {
                // TODOx
                IOE.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
}
