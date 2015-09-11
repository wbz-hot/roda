/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import pt.gov.dgarq.roda.core.common.AuthorizationDeniedException;
import pt.gov.dgarq.roda.core.common.RODAException;
import pt.gov.dgarq.roda.core.data.adapter.ContentAdapter;
import pt.gov.dgarq.roda.core.data.adapter.facet.Facets;
import pt.gov.dgarq.roda.core.data.adapter.filter.Filter;
import pt.gov.dgarq.roda.core.data.adapter.sort.Sorter;
import pt.gov.dgarq.roda.core.data.adapter.sublist.Sublist;
import pt.gov.dgarq.roda.core.data.v2.Group;
import pt.gov.dgarq.roda.core.data.v2.IndexResult;
import pt.gov.dgarq.roda.core.data.v2.LogEntry;
import pt.gov.dgarq.roda.core.data.v2.RODAMember;
import pt.gov.dgarq.roda.core.data.v2.RodaGroup;
import pt.gov.dgarq.roda.core.data.v2.User;
import pt.gov.dgarq.roda.wui.common.client.GenericException;
import pt.gov.dgarq.roda.wui.common.client.PrintReportException;

/**
 * @author Luis Faria
 * 
 */
public interface UserManagementService extends RemoteService {

  /**
   * Service URI path
   */
  public static final String SERVICE_URI = "UserManagementService";

  /**
   * Factory utility
   */
  public static class Util {

    /**
     * Get a new instance of the service
     * 
     * @return the instance
     */
    public static UserManagementServiceAsync getInstance() {

      UserManagementServiceAsync instance = (UserManagementServiceAsync) GWT.create(UserManagementService.class);
      ServiceDefTarget target = (ServiceDefTarget) instance;
      target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
      return instance;
    }
  }

  public Long getMemberCount(Filter filter) throws AuthorizationDeniedException, GenericException;

  public IndexResult<RODAMember> findMembers(Filter filter, Sorter sorter, Sublist sublist, Facets facets,
    String localeString) throws AuthorizationDeniedException, GenericException;

  /**
   * Get a group
   * 
   * @param groupname
   *          the group name
   * @return the group
   * @throws RODAException
   */
  public RodaGroup getGroup(String groupname) throws RODAException;

  /**
   * Get a user
   * 
   * @param username
   *          the user name
   * @return the user
   * 
   * @throws RODAException
   */
  public User getUser(String username) throws RODAException;

  /**
   * Create a new user
   * 
   * @param user
   *          the user
   * @param password
   *          the user password
   * @throws RODAException
   */
  public void createUser(User user, String password) throws RODAException;

  /**
   * Modify a user
   * 
   * @param user
   *          the modified users
   * @param password
   *          the new user password, or null to stay the same
   * @throws RODAException
   * 
   */
  public void editUser(User user, String password) throws RODAException;

  /**
   * Modify the authenticated user
   * 
   * @param user
   *          the modified user
   * @param password
   *          the user password if modified, or null if it remains the same
   * @throws RODAException
   */
  public void editMyUser(User user, String password) throws RODAException;

  /**
   * Create a group
   * 
   * @param group
   *          the new group
   * @throws RODAException
   */
  public void createGroup(Group group) throws RODAException;

  /**
   * Modify a group
   * 
   * @param group
   *          the modified group
   * @throws RODAException
   */
  public void editGroup(Group group) throws RODAException;

  /**
   * Try to remove a user, if user cannot be removed it will be deactivated
   * 
   * @param username
   *          the user name
   * @return true if user was removed, false if it was only deactivated
   * @throws RODAException
   */
  public boolean removeUser(String username) throws RODAException;

  /**
   * Remove a group
   * 
   * @param groupname
   *          the group name
   * @throws RODAException
   */
  public void removeGroup(String groupname) throws RODAException;

  /**
   * Get the number log entries
   * 
   * @param filter
   * @return
   * @throws RODAException
   */
  public Long getLogEntriesCount(Filter filter) throws RODAException;

  public IndexResult<LogEntry> findLogEntries(Filter filter, Sorter sorter, Sublist sublist, Facets facets)
    throws AuthorizationDeniedException, GenericException;

  /**
   * Register a new user
   * 
   * @param user
   *          The user to register
   * @param password
   *          user password
   * @param captcha
   *          the captcha chalenge
   * @return true if passed the chalenge, false otherwise
   * @throws RODAException
   */
  public boolean register(User user, String password, String captcha) throws RODAException;

  /**
   * Verify a user email. If verified user will become active
   * 
   * @param username
   *          the name of the user
   * @param token
   *          the token used in email verification
   * @return true if email verified, false otherwise
   * @throws RODAException
   * 
   */
  public boolean verifyemail(String username, String token) throws RODAException;

  /**
   * Resend the email chalenge to a user email
   * 
   * @param username
   *          the name of the user
   * @return true if email resent, false otherwise
   * @throws RODAException
   * 
   */
  public boolean resendEmailVerification(String username) throws RODAException;

  /**
   * Change the email of a user that is still not active due to a email
   * unverified
   * 
   * @param username
   *          the name of the user
   * @param email
   *          the new email
   * @return true if email was successfully changed, false otherwise
   * @throws RODAException
   * 
   */
  public boolean changeUnverifiedEmail(String username, String email) throws RODAException;

  /**
   * Request to reset the password. An email will be sent to the user with the
   * password reset token.
   * 
   * @param usernameOrEmail
   *          the user name or email
   * @param captcha
   *          the capcha chalenge answer
   * @return true if the user passed the chalenge, false otherwise
   * @throws RODAException
   */
  public boolean requestPassordReset(String usernameOrEmail, String captcha) throws RODAException;

  /**
   * Reset a user password
   * 
   * @param username
   *          the user name
   * @param resetPasswordToken
   *          the password token that was sent by email on
   *          requestPasswordReset(String, String)
   * @param newPassword
   *          the new password
   * @throws RODAException
   * 
   */
  public void resetPassword(String username, String resetPasswordToken, String newPassword) throws RODAException;

  /**
   * Set user log report info
   * 
   * @param adapter
   * @param localeString
   * @throws PrintReportException
   */
  public void setUserLogReportInfo(ContentAdapter adapter, String localeString) throws PrintReportException;

}
