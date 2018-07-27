/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.index.filter.BasicSearchFilterParameter;
import org.roda.core.data.v2.index.filter.Filter;
import org.roda.core.data.v2.index.filter.FilterParameter;
import org.roda.core.data.v2.index.filter.OrFiltersParameters;
import org.roda.core.data.v2.index.filter.SimpleFilterParameter;
import org.roda.wui.client.common.lists.utils.AsyncTableCell;
import org.roda.wui.client.common.utils.JavascriptUtils;
import org.roda.wui.common.client.widgets.wcag.AccessibleFocusPanel;
import org.roda.wui.common.client.widgets.wcag.WCAGUtilities;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class SearchPanel extends Composite implements HasValueChangeHandlers<String> {
  private static final String FILTER_ICON = "<i class='fa fa-filter' aria-hidden='true'></i>";

  private static final ClientMessages messages = GWT.create(ClientMessages.class);
  private static final Binder binder = GWT.create(Binder.class);

  interface Binder extends UiBinder<Widget, SearchPanel> {
  }

  @UiField
  FlowPanel searchPanel;

  @UiField
  SelectedPanel searchSelectedPanel;

  @UiField
  Dropdown searchInputListBox;

  @UiField
  TextBox searchInputBox;

  @UiField
  AccessibleFocusPanel searchInputButton;

  @UiField
  AccessibleFocusPanel searchAdvancedDisclosureButton;

  @UiField
  FlowPanel searchAdvancedPanel;

  @UiField
  FlowPanel searchAdvancedPanelButtons;

  @UiField
  Button searchAdvancedFieldOptionsAdd;

  @UiField
  Button searchAdvancedGo;

  @UiField
  Button searchAdvancedClean;

  @UiField
  FlowPanel searchPreFilters;

  private Filter defaultFilter;
  private String allFilter;
  private boolean defaultFilterIncremental = false;

  private FlowPanel fieldsPanel;
  private AsyncTableCell<IsIndexed, ?> list;

  private boolean hidePreFilters;

  public SearchPanel(Filter defaultFilter, String allFilter, boolean incremental, String placeholder,
    boolean showSearchInputListBox, boolean showSearchAdvancedDisclosureButton, boolean hidePreFilters) {
    this.defaultFilter = defaultFilter;
    this.allFilter = allFilter;
    this.hidePreFilters = hidePreFilters;
    this.defaultFilterIncremental = incremental;

    initWidget(binder.createAndBindUi(this));

    if (placeholder != null) {
      searchInputBox.getElement().setPropertyString("placeholder", placeholder);
    }

    searchSelectedPanel.setVisible(false);
    searchInputListBox.setVisible(showSearchInputListBox);
    searchAdvancedDisclosureButton.setVisible(showSearchAdvancedDisclosureButton);
    searchAdvancedPanel.setVisible(false);

    searchInputBox.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          doSearch();
          onChange(RodaConstants.SEARCH_BUTTON_EVENT_MARK);
        }
      }
    });

    searchInputButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSearch();
        onChange(RodaConstants.SEARCH_BUTTON_EVENT_MARK);
      }
    });

    searchAdvancedDisclosureButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showSearchAdvancedPanel();
      }
    });

    searchInputListBox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        onChange(searchInputListBox.getSelectedValue());
      }
    });

    searchSelectedPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        searchSelectedPanel.setVisible(event.getValue());
        searchInputListBox.setVisible(!event.getValue() && showSearchInputListBox);
      }
    });

    if (showSearchAdvancedDisclosureButton) {
      searchPanel.addStyleName("searchPanelAdvanced");
    }

    if (!hidePreFilters) {
      drawSearchPreFilters();
    }

    WCAGUtilities.getInstance().makeAccessible(searchInputListBox.getElement());
  }

  private void drawSearchPreFilters() {
    searchPreFilters.clear();
    searchPreFilters.setVisible(defaultFilter != null && !defaultFilter.getParameters().isEmpty());

    if (defaultFilter != null) {
      List<FilterParameter> parameters = defaultFilter.getParameters();
      for (int i = 0; i < parameters.size(); i++) {
        SafeHtml filterHTML = SearchPreFilterUtils.getFilterParameterHTML(parameters.get(i));

        if (filterHTML != null) {
          if (i == 0) {
            HTML header = new HTML(SafeHtmlUtils.fromSafeConstant(FILTER_ICON));
            header.addStyleName("inline gray");
            searchPreFilters.add(header);
          } else {
            InlineHTML andSeparator = new InlineHTML(messages.searchPreFilterAnd());
            andSeparator.addStyleName("xsmall gray");
            searchPreFilters.add(andSeparator);
          }

          HTML html = new HTML(filterHTML);
          html.addStyleName("xsmall gray inline nowrap");
          searchPreFilters.add(html);
        }
      }
    }
  }

  public void doSearch() {
    Filter filter = buildSearchFilter(searchInputBox.getText(), defaultFilter, allFilter, fieldsPanel,
      defaultFilterIncremental);
    list.setFilter(filter);
  }

  private Filter buildSearchFilter(String basicQuery, Filter defaultFilter, String allFilter, FlowPanel fieldsPanel,
    boolean defaultFilterIncremental) {
    List<FilterParameter> parameters = new ArrayList<>();
    Map<String, FilterParameter> advancedSearchFilters = new HashMap<>();

    if (basicQuery != null && basicQuery.trim().length() > 0) {
      parameters.add(new BasicSearchFilterParameter(allFilter, basicQuery));
    }

    if (fieldsPanel != null && fieldsPanel.getParent() != null && fieldsPanel.getParent().isVisible()) {
      for (int i = 0; i < fieldsPanel.getWidgetCount(); i++) {
        if (fieldsPanel.getWidget(i) instanceof SearchFieldPanel) {
          SearchFieldPanel searchAdvancedFieldPanel = (SearchFieldPanel) fieldsPanel.getWidget(i);
          String searchFieldId = searchAdvancedFieldPanel.getSearchField().getId();
          FilterParameter oldFilterParameter = advancedSearchFilters.get(searchFieldId);
          FilterParameter filterParameter = searchAdvancedFieldPanel.getFilter();

          if (filterParameter instanceof SimpleFilterParameter) {
            SimpleFilterParameter parameter = (SimpleFilterParameter) filterParameter;
            if (RodaConstants.AIP_LEVEL.equals(parameter.getName())
              && RodaConstants.NONE_SELECTED_LEVEL.equals(parameter.getValue())) {
              filterParameter = null;
            }
          }

          if (filterParameter != null) {
            if (oldFilterParameter != null) {
              if (oldFilterParameter instanceof OrFiltersParameters) {
                List<FilterParameter> filterParameters = ((OrFiltersParameters) oldFilterParameter).getValues();
                filterParameters.add(filterParameter);
                ((OrFiltersParameters) oldFilterParameter).setValues(filterParameters);
                advancedSearchFilters.put(searchFieldId, oldFilterParameter);
              } else {
                List<FilterParameter> filterParameters = new ArrayList<>();
                filterParameters.add(oldFilterParameter);
                filterParameters.add(filterParameter);
                advancedSearchFilters.put(searchFieldId, new OrFiltersParameters(filterParameters));
              }
            } else {
              advancedSearchFilters.put(searchFieldId, filterParameter);
            }
          }
        }
      }

      for (FilterParameter value : advancedSearchFilters.values()) {
        parameters.add(value);
      }
    }

    Filter filter;
    if (defaultFilterIncremental) {
      filter = defaultFilter != null ? new Filter(defaultFilter) : new Filter();
      filter.add(parameters);
      searchPreFilters.setVisible(!filter.getParameters().isEmpty());
    } else if (parameters.isEmpty()) {
      filter = defaultFilter;
      searchPreFilters.setVisible(filter != null && !filter.getParameters().isEmpty());
    } else {
      filter = new Filter(parameters);
      searchPreFilters.setVisible(false);
    }
    return filter;
  }

  public String getDropdownSelectedValue() {
    return searchInputListBox.getSelectedValue();
  }

  public boolean setDropdownSelectedValue(String value) {
    return setDropdownSelectedValue(value, true);
  }

  public boolean setDropdownSelectedValue(String value, boolean fire) {
    return searchInputListBox.setSelectedValue(value, fire);
  }

  public void setDropdownLabel(String label) {
    searchInputListBox.setLabel(label);
  }

  public void addDropdownItem(String label, String value) {
    searchInputListBox.addItem(label, value);
  }

  private void showSearchAdvancedPanel() {
    searchAdvancedPanel.setVisible(!searchAdvancedPanel.isVisible());
    if (searchAdvancedPanel.isVisible()) {
      searchAdvancedDisclosureButton.addStyleName("open");
    } else {
      searchAdvancedDisclosureButton.removeStyleName("open");
    }
  }

  public void addDropdownPopupStyleName(String styleName) {
    searchInputListBox.addPopupStyleName(styleName);
  }

  public void setFieldsPanel(FlowPanel fieldsPanel) {
    this.fieldsPanel = fieldsPanel;
    searchAdvancedPanel.clear();
    searchAdvancedPanel.add(fieldsPanel);
    searchAdvancedPanel.add(searchAdvancedPanelButtons);
  }

  @SuppressWarnings("unchecked")
  public void setList(AsyncTableCell<?, ?> list) {
    this.list = (AsyncTableCell<IsIndexed, ?>) list;
    searchSelectedPanel.bindList(this.list);
  }

  public void setDefaultFilter(Filter defaultFilter, boolean incremental) {
    this.defaultFilter = defaultFilter;
    if (!hidePreFilters) {
      drawSearchPreFilters();
    }
    this.defaultFilterIncremental = incremental;
  }

  public void setAllFilter(String allFilter) {
    this.allFilter = allFilter;
  }

  public void setVariables(Filter defaultFilter, String allFilter, boolean incremental, AsyncTableCell<?, ?> list,
    FlowPanel fieldsPanel) {
    setDefaultFilter(defaultFilter, incremental);
    setAllFilter(allFilter);
    setList(list);
    setFieldsPanel(fieldsPanel);
  }

  public void setDefaultFilterIncremental(boolean incremental) {
    this.defaultFilterIncremental = incremental;
  }

  public boolean isDefaultFilterIncremental() {
    return defaultFilterIncremental;
  }

  public void clearSearchInputBox() {
    searchInputBox.setText("");
  }

  public void setSearchAdvancedFieldOptionsAddVisible(boolean visible) {
    searchAdvancedFieldOptionsAdd.setVisible(visible);
  }

  public void setSearchAdvancedGoEnabled(boolean enabled) {
    searchAdvancedGo.setEnabled(enabled);
  }

  public void addSearchAdvancedFieldAddHandler(ClickHandler handler) {
    searchAdvancedFieldOptionsAdd.addClickHandler(handler);
  }

  public void hidePreFilters() {
    searchPreFilters.clear();
    searchPreFilters.setVisible(false);
  }

  @UiHandler("searchAdvancedClean")
  void handleSearchAdvancedClean(ClickEvent e) {
    JavascriptUtils.cleanAdvancedSearch();
  }

  @UiHandler("searchAdvancedGo")
  void handleSearchAdvancedGo(ClickEvent e) {
    doSearch();
    onChange(RodaConstants.SEARCH_BUTTON_EVENT_MARK);
  }

  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  protected void onChange(String value) {
    ValueChangeEvent.fire(this, value);
  }

  public void addKeyDownEvent(KeyDownHandler handler) {
    searchInputBox.addKeyDownHandler(handler);
  }
}
