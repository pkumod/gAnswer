package fgmt;

public abstract class Fragment {
	public enum typeEnum {ENTITY_FRAGMENT, RELATION_FRAGMENT, TYPE_FRAGMENT, VAR_FRAGMENT}; 
	
	public typeEnum fragmentType;
	public int fragmentId;
};
