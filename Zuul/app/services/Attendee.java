package services;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.carrot2.core.LanguageCode;
import org.carrot2.text.linguistic.DefaultStemmerFactory;
import org.carrot2.text.linguistic.IStemmer;
import org.codehaus.jackson.map.ObjectMapper;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Attendee {

  public static class Attendant implements Serializable {
    private static final long serialVersionUID = 6328662836076246928L;

    public static class Ticket implements Serializable {
      private static final long serialVersionUID = 6328662836076246928L;
      private String lastName;
      private Boolean personalized;
      private String displayIdentifier;
      private String paymentId;
      private int amiandoFee;
      private String cancelled;

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
          userData = new ArrayList<Attendee.Attendant.Ticket.Tuple>();
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

  private String path;

  public void setDbPath(String path) {
    this.path = path;
  }

  private List<Attendant> attendees;

  public boolean createAttendant(final Attendant attendant) {
    boolean found = CollectionUtils.select(attendees, new Predicate() {

      @Override
      public boolean evaluate(Object arg0) {
        Attendant obj = (Attendant) arg0;
        return obj.ticket.firstName != null
            && obj.ticket.firstName.equals(attendant.ticket.firstName)
            && obj.ticket.lastName != null
            && obj.ticket.lastName.equals(attendant.ticket.lastName);

        /* && email != null && email.matches("\\s+\\@\\s+") */
      }
    }).size() > 0;
    if (found) {
      long val = new Double((Math.random() * 1000000000000L)+1000000000000L).longValue();
      String str = new Long(val).toString();
      attendant.ticket.id = val;
      attendant.ticket.setDisplayIdentifier(str.substring(str.length()-4)+"-"+
                                            str.substring(str.length()-8, str.length()-4)+"-"+
                                            str.substring(str.length()-12, str.length()-8));
      attendees.add(attendant);
    }
    return !found;
  }

  public Collection<Attendant.Ticket> find(String str) {
    if (attendees == null) {
      init();
    }
    class Stemmer {
        final IStemmer stemmer = new DefaultStemmerFactory().getStemmer(LanguageCode.GERMAN);
    	public String stem(String stem) {
    		CharSequence ret = stemmer.stem(stem);
    		if (ret == null) {
    			return stem;
    		}
    		return ret.toString();
    	}
    };
    final Stemmer stemmer = new Stemmer();   
    final Collection<String> search = CollectionUtils.collect(
        Arrays.asList(str.toLowerCase().replace("-", "").split("\\s+")),
        new Transformer() {          
          @Override
          public Object transform(Object arg0) {
            String ret = stemmer.stem((String)arg0);
            System.out.println("stem:"+ret);
            return ret;
          }
        });
    
    return CollectionUtils.collect(
        CollectionUtils.select(attendees, new Predicate() {

          @Override
          public boolean evaluate(Object arg0) {
            final Attendant attendant = ((Attendant) arg0);
            final Attendant.Ticket ticket = attendant.getTicket();
            for (String s : search) {
              if (ticket.getEmail() != null) {
                for (String n : ticket.getEmail().toLowerCase().split("@")) {
                  if (stemmer.stem(n).startsWith(s)) {
                    return true;
                  }
                }
              }
              for (String n : ticket.getLastName().toLowerCase().split("\\s+")) {
                if (stemmer.stem(n).startsWith(s)) {
                  return true;
                }
              }
              for (String n : ticket.getFirstName().toLowerCase().split("\\s+")) {
                if (stemmer.stem(n).startsWith(s)) {
                  return true;
                }
              }
              if (Long.toString(ticket.getId()).startsWith(s)) {
                return true;
              }
              if (ticket.getDisplayIdentifier().replace("-", "").startsWith(s)) {
                return true;
              }
            }
            return false;
          }
        }), new Transformer() {

          @Override
          public Object transform(Object arg0) {
            // TODO Auto-generated method stub
            return ((Attendant) arg0).getTicket();
          }
        });
  }

  private void init() {
    ObjectMapper om = new ObjectMapper();
    attendees = new ArrayList<Attendant>();
    for (File f : new File(path).listFiles()) {
      if (f.getName().endsWith(".ticket.json")) {
        try {
          attendees.add(om.readValue(f, Attendant.class));
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    System.err.println("COUNT:" + attendees.size());
  }
}
