package com.SEGroup.UI;

import com.SEGroup.DTO.AuctionDTO;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple in-memory cache so NotificationView can see end-time. */
public final class ProductCache {
    private static final Map<String, AuctionDTO> map = new ConcurrentHashMap<>();
    private ProductCache() { }
    public static void put(String pid, AuctionDTO a){ map.put(pid, a); }
    public static AuctionDTO getAuction(String pid){ return map.get(pid); }
}
