package vo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;


public class SubjectMouldVO {
	
	public long sid;
	public long smid;
	public long mouldId;
	public String mname;
	public int nsort;
	public String flag;
	public String manType;
	
	public List<SubjectMouldProVO> subjectMouldProVOs = new ArrayList<SubjectMouldProVO>();
}
