package services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.ektorp.support.CouchDbDocument;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Attendant extends CouchDbDocument {
  private static final long serialVersionUID = 6328662836076246928L;

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Ticket implements Serializable {
    private static final long serialVersionUID = 6328662836076246928L;
    private String lastName;
    private Boolean personalized;
    private String displayIdentifier;
    private String paymentId;
    private int amiandoFee;
    private String cancelled;

    @JsonIgnore
    public boolean isValid() {
      return firstName != null && firstName.length() >= 2 && lastName != null
          && lastName.length() >= 2 /*
                                     * && email != null &&
                                     * email.matches("\\s+\\@\\s+")
                                     */;
    }

    public String getCancelled() {
      return cancelled;
    }

    public void setCancelled(String cancelled) {
      this.cancelled = cancelled;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Tuple implements Serializable {
      private static final long serialVersionUID = 6328662836076246928L;

      public String getTitle() {
        return title;
      }

      public void setTitle(String title) {
        this.title = title;
      }

      public String getValue() {
        return value;
      }

      public void setValue(String value) {
        this.value = value;
      }

      public String getType() {
        return type;
      }

      public void setType(String type) {
        this.type = type;
      }

      private String title;
      private String value;
      private String type;
    }

    private List<Tuple> userData;
    private int discountAmount;
    private int ticketFee;
    private long id;
    private int originalPrice;
    private String email;
    private String ticketType;
    private int productFee;
    private String firstName;
    private Boolean checked;
    private int productPrice;
    private long identifier;
    private List<Integer> ticketCategoryIds;
    private int ticketRevenue;

    public void setCompany(String str) {
      if (userData == null) {
        userData = new ArrayList<Attendant.Ticket.Tuple>();
      }
      for (Tuple i : userData) {
        if (i.getTitle().equals("Company")) {
          i.setValue(str);
          return;
        }
      }
      Tuple tuple = new Tuple();
      tuple.setTitle("Company");
      tuple.setValue(str);
      userData.add(tuple);

    }

    @JsonIgnore
    public String getCompany() {
      for (Tuple i : userData) {
        if (i.getTitle().equals("Company")) {
          return i.getValue();
        }
      }
      return null;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public Boolean getPersonalized() {
      return personalized;
    }

    public void setPersonalized(Boolean personalized) {
      this.personalized = personalized;
    }

    public String getDisplayIdentifier() {
      return displayIdentifier;
    }

    public void setDisplayIdentifier(String displayIdentifier) {
      this.displayIdentifier = displayIdentifier;
    }
    
    @JsonIgnore
    public String getShortDisplayIdentifier() {
      StringBuilder buffer = new StringBuilder();
      for(String i : displayIdentifier.split("-")) {
        buffer.append(i);
      }
      return buffer.toString();
    }

    public String getPaymentId() {
      return paymentId;
    }

    public void setPaymentId(String paymentId) {
      this.paymentId = paymentId;
    }

    public int getAmiandoFee() {
      return amiandoFee;
    }

    public void setAmiandoFee(int amiandoFee) {
      this.amiandoFee = amiandoFee;
    }

    public List<Tuple> getUserData() {
      return userData;
    }

    public void setUserData(List<Tuple> userData) {
      this.userData = userData;
    }

    public int getDiscountAmount() {
      return discountAmount;
    }

    public void setDiscountAmount(int discountAmount) {
      this.discountAmount = discountAmount;
    }

    public int getTicketFee() {
      return ticketFee;
    }

    public void setTicketFee(int ticketFee) {
      this.ticketFee = ticketFee;
    }

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public int getOriginalPrice() {
      return originalPrice;
    }

    public void setOriginalPrice(int originalPrice) {
      this.originalPrice = originalPrice;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getTicketType() {
      return ticketType;
    }

    public void setTicketType(String ticketType) {
      this.ticketType = ticketType;
    }

    public int getProductFee() {
      return productFee;
    }

    public void setProductFee(int productFee) {
      this.productFee = productFee;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public Boolean getChecked() {
      return checked;
    }

    public void setChecked(Boolean checked) {
      this.checked = checked;
    }

    public int getProductPrice() {
      return productPrice;
    }

    public void setProductPrice(int productPrice) {
      this.productPrice = productPrice;
    }

    public long getIdentifier() {
      return identifier;
    }

    public void setIdentifier(long identifier) {
      this.identifier = identifier;
    }

    public List<Integer> getTicketCategoryIds() {
      return ticketCategoryIds;
    }

    public void setTicketCategoryIds(List<Integer> ticketCategoryIds) {
      this.ticketCategoryIds = ticketCategoryIds;
    }

    public int getTicketRevenue() {
      return ticketRevenue;
    }

    public void setTicketRevenue(int ticketRevenue) {
      this.ticketRevenue = ticketRevenue;
    }

  }

  public static String longId(String id) {
    return id.substring(0, 4)+"-"+id.substring(4,8)+"-"+id.substring(8);
  }
  
  public Ticket getTicket() {
    return ticket;
  }

  public void setTicket(Ticket ticket) {
    this.ticket = ticket;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  private Ticket ticket;
  private Boolean success;
}

