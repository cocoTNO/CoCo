package net.geant.coco.agent.portal.rest;

public class RestVpnURIConstants {
	 
    public static final String DUMMY_VPN = "/rest/vpn/dummy";
    public static final String INVITE_VPN = "/rest/vpn/invite";
    public static final String ACCEPT_VPN = "/rest/vpn/accept";
    public static final String GET_VPN = "/rest/vpn/{id}";
    public static final String UPDATE_VPN = "/rest/vpn/update/{id}";
    public static final String SET_VPN_PRIVACY = "/rest/vpn/setPrivacy/{id}";
    public static final String GET_ALL_VPN = "/rest/vpns";
    public static final String CREATE_VPN = "/rest/vpn/add";
    public static final String DELETE_VPN = "/rest/vpn/del/{id}";
    public static final String GET_ALL_SITES = "/rest/sites/{id}";
    public static final String UPDATE_BGP = "/rest/bgp/update";
    
    
    
    public static final String GET_USER_SUBNETS = "/rest/subnets";
    
    public static final String GET_TOPOLOGY = "/topology";
    public static final String GET_TOPOLOGY_VIS = "/topology/vis";
    public static final String GET_TOPOLOGY_IS_CHANGED = "/topology/isChanged";
}
