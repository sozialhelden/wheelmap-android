package wheelmap.org.domain.nodetype;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import wheelmap.org.domain.node.Category;

@JsonAutoDetect
public class NodeType {

	private BigDecimal id;
	private String identifier;
	@JsonProperty( value="icon" )
	private String iconUrl;
	@JsonIgnore
	private byte[] iconData;
	@JsonProperty( value="localized_name")
	private String localizedName;
	@JsonProperty( value="category_id")
	private BigDecimal categoryId;
	
	
	// This Category should be there according to the docs. But in fact its not there
	@JsonIgnore
	private Category category;
	
	public BigDecimal getId() {
		return id;
	}
	
	public void setId( BigDecimal id ) {
		this.id= id;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier( String identifier ) {
		this.identifier = identifier;
	}
	
	public String getIconUrl() {
		return iconUrl;
	}
	
	public void setIconUrl( String url ) {
		this.iconUrl = url;
	}
	
	public byte[] getIconData() {
		return iconData;
	}
	
	public void setIconData( byte[] data ) {
		this.iconData = data;
	}
	
	public String getLocalizedName() {
		return localizedName;
	}
	
	public void setLocalizedName( String localizedName ) {
		this.localizedName = localizedName;
	}
	
	public BigDecimal getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId( BigDecimal id ) {
		this.categoryId = id;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory( Category category ) {
		this.category = category;
	}
}
