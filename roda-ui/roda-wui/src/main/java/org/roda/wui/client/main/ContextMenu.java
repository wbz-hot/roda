/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.main;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.select.SelectedItemsList;
import org.roda.wui.client.common.actions.Actionable;
import org.roda.wui.client.common.popup.CalloutPopup;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ContextMenu<T extends IsIndexed> extends Composite implements HasHandlers {
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  interface MyUiBinder extends UiBinder<Widget, ContextMenu> {
  }

  private final CalloutPopup actionsPopup = new CalloutPopup();
  private Actionable<T> actionable = null;

  @UiField
  AccessibleFocusPanel button;

  public ContextMenu() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public void init(Actionable<T> actionable) {
    this.actionable = actionable;
  }

  @UiHandler("button")
  void handleLogin(ClickEvent e) {
    showActions();
  }

  protected void showActions() {
    if (actionable != null) {
      if (actionsPopup.isShowing()) {
        actionsPopup.hide();
      } else {
        actionsPopup.showRelativeTo(button, CalloutPopup.CalloutPosition.TOP_RIGHT);
      }
    }
  }

  public void setPopupWidget(Widget w){
    actionsPopup.setWidget(w);
  }
}
