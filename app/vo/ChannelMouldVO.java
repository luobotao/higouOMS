package vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;


public class ChannelMouldVO {
	
	public long cid;
	public long cmid;
	public long mouldId;
	public String mname;
	public int nsort;
	public String flag;
	public String manType;
	
	public List<ChannelMouldProVO> channelMouldProVOs = new ArrayList<ChannelMouldProVO>();
}
