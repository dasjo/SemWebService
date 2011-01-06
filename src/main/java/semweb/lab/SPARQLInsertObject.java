package semweb.lab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sparql_object")
@XmlAccessorType(XmlAccessType.FIELD)
public class SPARQLInsertObject {
  
  protected String resourceURI;
  protected String propertyNameSpace;
  protected String propertyLocalName;
  protected Object object;
  protected boolean isLiteral;
  
  
  public String getResourceURI() {
    return resourceURI;
  }
  public void setResourceURI(String resourceURI) {
    this.resourceURI = resourceURI;
  }
  public String getPropertyNameSpace() {
    return propertyNameSpace;
  }
  public void setPropertyNameSpace(String propertyNameSpace) {
    this.propertyNameSpace = propertyNameSpace;
  }
  public String getPropertyLocalName() {
    return propertyLocalName;
  }
  public void setPropertyLocalName(String propertyLocalName) {
    this.propertyLocalName = propertyLocalName;
  }
  public Object getObject() {
    return object;
  }
  public void setObject(Object object) {
    this.object = object;
  }
  public boolean isLiteral() {
    return isLiteral;
  }
  public void setLiteral(boolean isLiteral) {
    this.isLiteral = isLiteral;
  }
  
  
  
  
}
