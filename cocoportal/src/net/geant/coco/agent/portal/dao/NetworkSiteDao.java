package net.geant.coco.agent.portal.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NetworkSiteDao {
    private NamedParameterJdbcTemplate jdbc;
    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setDataSource(DataSource jdbc) {
        this.jdbc = new NamedParameterJdbcTemplate(jdbc);
    }
     
    private NetworkSite extractDataNS (ResultSet rs) throws SQLException {
    	NetworkSite networkSite = new NetworkSite();
    	
    	ResultSetMetaData rsMeta = rs.getMetaData();
    	int numberOfColumns = rsMeta.getColumnCount();
    	
    	log.info("NetworkSite.extractData with " + numberOfColumns + " columns");
    	
    	// get the column names; column indexes start from 1
    	for (int i = 1; i < numberOfColumns + 1; i++) {
    	    String columnName = rsMeta.getColumnLabel(i);
    	    
    	    //log.info("NetworkSite.extractData label- " + rsMeta.getColumnLabel(i));
    	    
    	    switch (columnName) {
			case "id":
				networkSite.setId(rs.getInt("id"));
				break;
			/*case "name":
				networkSite.setName(rs.getString("name") + rs.getString("subnet"));
				break;*/
			case "switch_name":
				networkSite.setProviderSwitch(rs.getString("switch_name"));
				break;
			case "remote_port":
				networkSite.setProviderPort(rs.getInt("remote_port"));
				break;
			case "local_port":
				networkSite.setCustomerPort(rs.getInt("local_port"));
				break;
			case "vlanid":
				networkSite.setVlanId(rs.getInt("vlanid"));
				break;
			case "subnet":
				networkSite.setName(rs.getString("subnet"));
				networkSite.setIpv4Prefix(rs.getString("subnet"));
				break;
			/* this is depricated and should be replaced by the subnet*/
			case "ipv4prefix":
				//@Depricated
				if (networkSite.getIpv4Prefix() == null){
					networkSite.setIpv4Prefix(rs.getString("ipv4prefix"));
				}
				break;
			case "mac_address":
				networkSite.setMacAddress(rs.getString("mac_address"));
				break;
			case "vpn_name":
				networkSite.setVpnName(rs.getString("vpn_name"));
				break;
			case "userID":
				log.info("NetworkSite.extractData - userID");
				int userID = rs.getInt("userID");
				User user = this.userDao.getUser(userID);
				networkSite.setUser(user);
				log.info("NetworkSite.extractData - userID - " + user);
				break;
			default:
				break;
			}
    	}

        return networkSite;
    }

    public List<NetworkSite> getNetworkSites() {
        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
        		+ "vpnSubnet.user AS userID, "
                + "vpns.name AS vpn_name " + "FROM sites "
                + "INNER JOIN switches ON sites.switch = switches.id "
                + "INNER JOIN subnets ON sites.id=subnets.site "
                + "INNER JOIN vpnSubnet ON subnets.id = vpnSubnet.subnet "
                + "INNER JOIN vpns ON vpns.id = vpnSubnet.vpn;";
        // System.out.println(query);
        return jdbc.query(query, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = extractDataNS(rs);

                return networkSite;
            }

        });
    }
    
    

    //TODO: Simon: I have no idea why this is a list or why we combine the switch and network table here 
    public List<NetworkSite> getNetworkSites(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
        		+ "subnets.subnet AS subnet " + "FROM sites " 
        		+ "JOIN switches WHERE switch = switches.id "
                + "AND sites.id = :id;";
        
        return jdbc.query(query, params, new RowMapper<NetworkSite>() {
        	
            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = extractDataNS(rs);

                return networkSite;
            }

        });
    }

    public List<NetworkSite> getNetworkSites(String vpnName) {
        if (vpnName == null) {
            vpnName = "all";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn", vpnName);

        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
        		+ "vpnSubnet.user AS userID, "
                + "vpns.name AS vpn_name, " + "subnets.subnet AS subnet " + "FROM sites "
                + "INNER JOIN switches ON sites.switch = switches.id "
    			+ "INNER JOIN subnets ON sites.id = subnets.site "
                + "INNER JOIN vpnSubnet ON subnets.id = vpnSubnet.subnet "
                + "INNER JOIN vpns ON vpns.id = vpnSubnet.vpn "
                + "AND vpns.name = :vpn ;";
        log.info(query.replace(":vpn", vpnName));
        return jdbc.query(query, params, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
            	NetworkSite networkSite = extractDataNS(rs);

                return networkSite;
            }

        });
    }

    public List<NetworkSite> getNetworkSites(String siteName, String vpnName) {
        if (vpnName == null) {
            vpnName = "all";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vpn", vpnName);

        String query = "SELECT sites.*, " + "switches.name AS switch_name, "
        		+ "vpnSubnet.user AS userID, "
                + "vpns.name AS vpn_name, " + "subnets.subnet AS subnet " + "FROM sites "
                + "INNER JOIN switches ON switch = switches.id "
                + "INNER JOIN subnets ON sites.id = subnets.site "
                + "INNER JOIN vpnSubnet ON subnets.id = vpnSubnet.subnet "
                + "INNER JOIN vpns ON vpns.id = vpnSubnet.vpn "
                + "AND vpns.name = :vpn " + "AND switches.name = :sitename ;";
        return jdbc.query(query, params, new RowMapper<NetworkSite>() {

            @Override
            public NetworkSite mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                NetworkSite networkSite = extractDataNS(rs);

                return networkSite;
            }

        });
    }
    
    public NetworkSite getNetworkSite(int id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        String query = "SELECT sites.*, switches.name as switch_name FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.id = :id;";
        log.debug("getNetworkSite " + id + "  " + query);
        return jdbc.query(query, params, new ResultSetExtractor<NetworkSite>() {

            @Override
            public NetworkSite extractData(ResultSet rs) throws SQLException {               
                //TODO fix here, if there are no results
                rs.next();
                userDao.getUsers();
                NetworkSite networkSite = extractDataNS(rs);

                return networkSite;
            }

        });
    }
    
    public NetworkSite getExternalNetworkSite(Bgp bgp, Vpn vpn){

    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("domain_id", bgp.getRemote_domain_id());
        
        String query = "SELECT sites.*, switches.name as switch_name FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.domain = :domain_id;";
        
        log.debug("getNetworkSite " + query.replace(":domain_id", "" + bgp.getRemote_domain_id()));
        
        NetworkSite site = jdbc.query(query, params, new ResultSetExtractor<NetworkSite>() {

            @Override
            public NetworkSite extractData(ResultSet rs) throws SQLException {               
                //TODO fix here, if there are no results
                rs.next();
                
                NetworkSite networkSite = extractDataNS(rs);

                return networkSite;
            }

        });
        
        if (site == null){
        	return null;
        }
        
        // NOTE: THIS IS A HACK
        // What we do here is to abuse the site table in order to resolve the provider port for BGP vpns
        
        // replace the ID here, will will not make changes to this NetworkSite object
        site.setId(1000 + bgp.getId());
    	
    	String sitePrefix = bgp.getAnnounce();
    	site.setName("BGP" + sitePrefix);
    	site.setIpv4Prefix(sitePrefix);
    	
    	// TODO: check what we really want here as a type
    	site.nodeType = NetworkElement.NODE_TYPE.CUSTOMER_BGP;
    	
    	site.setUser(userDao.getDomainAdmin(bgp.getRemote_domain_id()));
    	
    	site.setVpnName(vpn.getName());
        
        return site;
    }

    public NetworkSite getNetworkSite(String subnet) {
    	// (Simon) we actually want the Subnet here not the site
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("subnet", subnet);
        
        String query = "SELECT sites.*, subnets.subnet AS subnet, switches.name as switch_name FROM sites "
                + "INNER JOIN switches ON sites.switch = switches.id "
                + "INNER JOIN subnets ON sites.id = subnets.site "
                + "WHERE subnets.subnet = :subnet;";
        log.debug("getNetworkSite " + query.replace(":subnet", subnet));
        return jdbc.query(query, params, new ResultSetExtractor<NetworkSite>() {

            @Override
            public NetworkSite extractData(ResultSet rs) throws SQLException {             
                //TODO fix here, if there are no results
                rs.next();
                
                return extractDataNS(rs);
            }

        });
    }
    
    // TODO: (Simon) this still needs to be fixed but i did not found essential code that uses that function - possible fix = delete
    @Deprecated
    public int insertNetworkSite(String name, int switchNumber, int remotePort, int localPort, int vlanId, String ipPrefix, String macAddress) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", name);
        params.addValue("switch", switchNumber);
        params.addValue("remote_port", remotePort);
        params.addValue("local_port", localPort);
        params.addValue("vlanid", vlanId);
        params.addValue("ipv4prefix", ipPrefix);
        params.addValue("mac_address", macAddress);

        String query = "INSERT INTO sites (name, switch, remote_port, local_port, vlanid, ipv4prefix, mac_address) "
        		+ "VALUES (:name, :switch, :remote_port, :local_port, :vlanid, :ipv4prefix, :mac_address);";
        /*String query = "SELECT sites.* FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.name = :name;";*/
        log.debug("insertNetworkSite " + name + "  " + query);
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updateResultSites = jdbc.update(query, params, keyHolder);
        int newSiteId = keyHolder.getKey().intValue();
        
        params = new MapSqlParameterSource();
        params.addValue("site", newSiteId);
        params.addValue("switch", switchNumber);
        query = "INSERT INTO sitelinks (site, switch) VALUES "
        		+ "(:site, :switch);";
        
        int updateResultSiteLinks = jdbc.update(query, params);
        
        params = new MapSqlParameterSource();
        params.addValue("site", newSiteId);
        query = "INSERT INTO site2vpn (vpnid, siteid) VALUES "
        		+ "(1, :site);";
        
        int updateResultSiteToVpn = jdbc.update(query, params);
        
        return updateResultSites * updateResultSiteLinks * updateResultSiteToVpn;
    }
    
    public int deleteNetworkSite(String ipPrefix) {
    	MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ipv4prefix", ipPrefix);

        String query = "DELETE FROM  sites WHERE ipv4prefix = :ipv4prefix;";

        /*String query = "SELECT sites.* FROM sites "
                + "JOIN switches WHERE sites.switch = switches.id "
                + "AND sites.name = :name;";*/
        log.debug("deleteNetworkSite " + ipPrefix + "  " + query);
        
        return jdbc.update(query, params);
    }

	public int insertNetworkSite(String prefix, int vlanId, String neighborIp) {
		
		MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("neighbor_ip", neighborIp);
        
        //sites.name, sites.switch, sites.remote_port
        String query = "SELECT sites.name, sites.switch, sites.remote_port "
        		+ "FROM sites INNER JOIN ases ON sites.name = ases.as_name "
        		+ "WHERE ases.bgp_ip = :neighbor_ip;";
        
        
        class AsData {
            public String asSiteName;
            public int asSiteSwitch;
            public int asRemotePort;
        }
        
        AsData asData = jdbc.query(query, params, new ResultSetExtractor<AsData>() {

            @Override
            public AsData extractData(ResultSet rs) throws SQLException {
            	AsData asData = new AsData();
            	
            	if(rs.next()){
	            	asData.asSiteName = rs.getString("name");
			        asData.asSiteSwitch = rs.getInt("switch");
			        asData.asRemotePort = rs.getInt("remote_port");
            	}
            	
                return asData;
            }
        });
        
        String newSiteName = asData.asSiteName + "-" + prefix;
        this.insertNetworkSite(newSiteName, asData.asSiteSwitch, asData.asRemotePort, 1, vlanId, prefix, "00:00:00:00:00:00");
        
		return 0;
	}
}
