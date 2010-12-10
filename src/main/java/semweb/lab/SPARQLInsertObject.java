package semweb.lab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="sparql_object")
@XmlAccessorType(XmlAccessType.FIELD)
public class SPARQLInsertObject {
  
  protected String resource;
  protected String property;
  protected Object object;
  protected boolean isLiteral;
  
  public String getResource() {
    return resource;
  }
  public void setResource(String resource) {
    this.resource = resource;
  }
  public String getProperty() {
    return property;
  }
  public void setProperty(String property) {
    this.property = property;
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
