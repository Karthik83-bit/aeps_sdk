package com.example.aeps_sdk.vriddhi;

public interface IAemCardScanner {

public void onScanMSR(String buffer, com.example.aeps_sdk.vriddhi.CardReader.CARD_TRACK cardtrack);

public void onScanDLCard(String buffer);

public void onScanRCCard(String buffer);

public void onScanRFD(String buffer);

public void onScanPacket(String buffer);
}
