/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.actions;

import org.roda.core.data.v2.jobs.IndexedReport;
import org.roda.wui.client.common.actions.model.ActionsBundle;

import com.google.gwt.core.client.GWT;

import config.i18n.client.ClientMessages;

public class JobReportActions extends AbstractActionable<IndexedReport> {
  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final JobReportActions INSTANCE = new JobReportActions();

  private JobReportActions() {
  }

  public static JobReportActions get() {
    return INSTANCE;
  }

  @Override
  public ActionsBundle<IndexedReport> createActionsBundle() {
    // no actions
    return new ActionsBundle<>();
  }
}
