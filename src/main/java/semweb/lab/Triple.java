package semweb.lab;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="triple")
@XmlAccessorType(XmlAccessType.FIELD)
public class Triple {
  
  protected String subject;
  protected String predicate;
  protected Object object;
  protected boolean isLiteral;
  
  
  public String getSubject() {
    return subject;
  }
  public void setSubject(String subject) {
    this.subject = subject;
  }
  public String getPredicate() {
    return predicate;
  }
  public void setPredicate(String predicate) {
    this.predicate = predicate;
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
