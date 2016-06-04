/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.server;

import java.util.Date;

/**
 *
 * @author human
 */
public interface ChatMessageListener {

    void onMessage(String address, String message, Date timestamp);
}
