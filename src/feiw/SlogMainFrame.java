package feiw;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import feiw.LogSource.LogFilter;
import feiw.SystemConfigs.LogUrl;
import feiw.ToolBarDes.ToolItemDes;

public final class SlogMainFrame {
    

    private Display mDisplay;
    private Shell   mShell;

    
    CoolBar mCoolBar = null;    
    List<ToolItem> mToolItems = new ArrayList<ToolItem>(10);
    CTabFolder mTabFolder;
    
    public Display getDisplay() {
        return mDisplay;
    }
    public Shell getShell() {
        return mShell;
    }
    
    ToolItem getToolItem(String name) {
        for (ToolItem it : mToolItems) {
            if (name.equals(it.getData())) {
                return it;
            }
        }
         return null;
    }
    public SlogMainFrame(String caption, Display disp) {
        mDisplay = disp;
        mShell = new Shell(disp);
        mShell.setText(caption);
        createContents();
        mShell.addListener(SWT.Close, new Listener() {
           public void handleEvent(Event event) {
               closeTabFrames();
           }
         });
    }

    void createToolBar(ToolBarDes tbdes) {
        ToolBar tb = new ToolBar(mCoolBar, SWT.FLAT);
        tb.setData(tbdes.mName);
        for (ToolItemDes itdes : tbdes.mItems) {
            ToolItem it = new ToolItem(tb, itdes.mStyle);
            it.setData(itdes.mName);
            it.setData("KeyAccelerator", new Integer(itdes.mKeyAccelerator));
            it.setToolTipText(itdes.mTipText);
            if (itdes.mImage != null) {
                it.setImage(itdes.mImage);
                it.setDisabledImage(new Image(getDisplay(), itdes.mImage, SWT.IMAGE_GRAY));
            }
            mToolItems.add(it);
        }
        CoolItem item = new CoolItem(mCoolBar, SWT.NONE);
        Point p = tb.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        tb.setSize(p);
        Point p2 = item.computeSize(p.x, p.y);
        item.setControl(tb);
        item.setSize(p2);
        
    }
    
    abstract class DropdownListener extends SelectionAdapter {
        private ToolItem dropdown;

        private Menu menu;

        public abstract void init();
        public abstract void onToolSelected(ToolItem dropdown);
        
        public abstract void onListSelected(final Object o);
        
        public DropdownListener(ToolItem dropdown) {
          this.dropdown = dropdown;
          menu = new Menu(dropdown.getParent().getShell());
          init();
        }

        public void clearList() {
            menu.dispose();
            menu = new Menu(dropdown.getParent().getShell());
        }
        public void addListItem(String item, Image img, final Object o) {
          MenuItem menuItem = new MenuItem(menu, SWT.NONE);
          menuItem.setText(item);
          menuItem.setImage(img);
          menuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                onListSelected(o);
            }
          });
        }

        public void widgetSelected(SelectionEvent event) {
          if (!dropdown.getEnabled()) {
              return;
          }
          if (event.detail == SWT.ARROW) {
            ToolItem item = (ToolItem) event.widget;
            Rectangle rect = item.getBounds();
            Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
            menu.setLocation(pt.x, pt.y + rect.height);
            menu.setVisible(true);
          } else {
              onToolSelected(dropdown);
          }
        }
      }
    
   
    void createToolBars() {
        mCoolBar = new CoolBar(getShell(), SWT.FLAT);
        mCoolBar.setBackground(new Color(getDisplay(), 255,255,255));
        mCoolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
        for (ToolBarDes tdes : ToolBarDes.TOOBARS) {
            createToolBar(tdes);
        }

        getToolItem(ToolBarDes.TN_NEXT).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                    SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                    if (tbf != null) {
                        tbf.onNext();
                    }
            }
           
        });
        
        getToolItem(ToolBarDes.TN_PREV).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                    SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                    if (tbf != null) {
                        tbf.onPrev();
                    }
            }
           
        });
        
        
        getToolItem(ToolBarDes.TN_SEARCH).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SearchDlg d = new SearchDlg(Slogmain.getApp().getMainFrame().getShell());
                String txt = d.open();
                if (txt != null && !(txt.trim().isEmpty())) {
                    SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                    if (tbf != null) {
                        tbf.onSearch(txt.trim(),  d.isCaseSenstive());
                    }
                }
            }
           
        });
        

        getToolItem(ToolBarDes.TN_CONNECT).addSelectionListener(new DropdownListener(getToolItem(ToolBarDes.TN_CONNECT)) {
            @Override
            public void onToolSelected(ToolItem dropdown) {
                SystemConfigs.LogUrl lu = SystemConfigs.getRecentUrl(0);
                ConnectDlg dlg = new ConnectDlg(getShell(), lu.url, lu.port);
                if (dlg.open() == SWT.OK) {
                    QconnTabFrame ltab = new QconnTabFrame(mTabFolder, lu.toString(), SWT.FLAT|SWT.CLOSE|SWT.ICON, dlg.getIp(), dlg.getPort());
                    mTabFolder.setSelection(ltab);
                    SystemConfigs.addRecentUrl(new LogUrl("qconn", dlg.getIp(), dlg.getPort()));
                    init();
                    updateToolBars(ltab);
                }
            }

            @Override
            public void onListSelected(Object o) {
                if (o instanceof LogUrl) {
                    LogUrl lu = (LogUrl)o;
                    QconnTabFrame ltab = new QconnTabFrame(mTabFolder, lu.toString(), SWT.FLAT|SWT.CLOSE|SWT.ICON, lu.url, lu.port);
                    mTabFolder.setSelection(ltab);
                    updateToolBars(ltab);
                }

            }

            @Override
            public void init() {
                clearList();
                for (int i = 0; i < 5; i++) {
                     LogUrl lu = SystemConfigs.getRecentUrl(i);
                    if (lu != null) {
                        addListItem(lu.toString(), Resources.connected_16, lu);
                    }
                }
                
            }
        });
        
        getToolItem(ToolBarDes.TN_OPEN).addSelectionListener(new DropdownListener(getToolItem(ToolBarDes.TN_OPEN)){
 
            @Override
            public void onToolSelected(ToolItem dropdown) {
                FileDialog dialog = new FileDialog (getShell(), SWT.OPEN);
                String [] filterNames = new String [] {"Log Files", "All Files (*)"};
                String [] filterExtensions = new String [] {"*.log;*.txt;", "*"};
//                String filterPath = "";
                /*
                String platform = SWT.getPlatform();
                if (platform.equals("win32") || platform.equals("wpf")) {
                    filterNames = new String [] {"Image Files", "All Files (*.*)"};
                    filterExtensions = new String [] {"*.gif;*.png;*.bmp;*.jpg;*.jpeg;*.tiff", "*.*"};
                    filterPath = "c:\\";
                }
                */
                dialog.setFilterNames (filterNames);
                dialog.setFilterExtensions (filterExtensions);
            //    dialog.setFilterPath (filterPath);
                String fname = dialog.open();
                if (fname != null) {
                    FileTabFrame ftb = new FileTabFrame(mTabFolder, fname, SWT.FLAT|SWT.CLOSE|SWT.ICON, fname);
                    mTabFolder.setSelection(ftb);
                    
                    SystemConfigs.addRecentFile(fname);
                    init();
                    updateToolBars(ftb);
                    
                }
                
            }

            @Override
            public void onListSelected(Object o) {
               
                if (o instanceof String) {
                    String fname = (String)o;
                    FileTabFrame ftb = new FileTabFrame(mTabFolder, fname, SWT.FLAT|SWT.CLOSE|SWT.ICON, fname);
                    mTabFolder.setSelection(ftb);
                    updateToolBars(ftb);
                }
                
            }

            @Override
            public void init() {
                clearList();
                for (int i = 0; i < 5; i++) {
                    String fname = SystemConfigs.getRecentFile(i);
                    if (fname != null) {
                        addListItem(fname, Resources.openfile_16, fname);
                    }
                }
                
            }
        });
 
        getToolItem(ToolBarDes.TN_FILTER).addSelectionListener(new DropdownListener(getToolItem(ToolBarDes.TN_FILTER)) {
            private FilterTabFrame openFilterView(LogFilter f) {
                SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                FilterTabFrame ltab = new FilterTabFrame(mTabFolder, "\"" + f.getName() + "\" on [" + tbf.getText() + "]", SWT.FLAT|SWT.CLOSE|SWT.ICON, tbf.getLogSource(), 
                         f, tbf.getLogView());
                mTabFolder.setSelection(ltab);
                return ltab;
            }
            @Override
            public void onToolSelected(ToolItem dropdown) {
                FilterDlg fdlg = new FilterDlg(getShell());
                if (fdlg.open() != SWT.OK) {
                    return;
                }
                LogFilter f = fdlg.getFilter();
                FilterTabFrame tbf = openFilterView(f);
                SystemConfigs.addRecentFilter(f);
                init();
                updateToolBars(tbf);
            }

            @Override
            public void onListSelected(final Object o) {
                if (o instanceof LogFilter) {
                     updateToolBars(openFilterView((LogFilter)o));
                }
                
            }
            @Override
            public void init() {
                clearList();
                for (int i = 0; i < 5; i++) {
                     LogFilter f = SystemConfigs.getRecentFilter(i);
                    if (f != null) {
                        addListItem(f.getName(), Resources.filter_16, f);
                    }
                }
            }
            
        });
 
        getToolItem(ToolBarDes.TN_CLEAR).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                tbf.getLogView().clear();
            }
        });
        
        getToolItem(ToolBarDes.TN_PAUSE).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                if (tbf != null) {
                    tbf.onPause();
                }
            }
        });
        getToolItem(ToolBarDes.TN_DISCONNECT).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                if (tbf != null) {
                    tbf.onDisconnect();
                }
            }
        });
        getToolItem(ToolBarDes.TN_COPY).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                tbf.onCopy();
            }
        });
        getToolItem(ToolBarDes.TN_COPYALL).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                tbf.onCopyAll();
            }
        });
        
        getToolItem(ToolBarDes.TN_SAVEAS).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog (getShell(), SWT.SAVE);
                final String [] filterNames = new String [] {"Log Files", "All Files (*)"};
                final String [] filterExtensions = new String [] {"*.log;*.txt;", "*"};
                dialog.setFilterNames (filterNames);
                dialog.setFilterExtensions (filterExtensions);
                String fname = dialog.open();
                if (fname != null) {
                    SlogTabFrame tbf = (SlogTabFrame)mTabFolder.getSelection();
                    tbf.onSaveAs(fname);
                }
            }
        });
        
        getToolItem(ToolBarDes.TN_PREFERENCE).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox m = new MessageBox(getShell(), SWT.OK|SWT.ICON_INFORMATION);
                m.setText("Preference");
                m.setMessage("TODO.");
                m.open();
            }
        });
        getToolItem(ToolBarDes.TN_HELP).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox m = new MessageBox(getShell(), SWT.OK|SWT.ICON_INFORMATION);
                m.setText("About SuperLog");
                m.setMessage("TODO.");
                m.open();
            }
        });
    }
    
    void updateToolItem(ToolItem tit) {
        String tn = (String)tit.getData();
        if (tn == null || tn.isEmpty()) {
            tit.setEnabled(false);
            return;
        } 
        if (tn.equals(ToolBarDes.TN_CONNECT)) {
            tit.setEnabled(true);
        } else if (tn.equals(ToolBarDes.TN_OPEN)) {
            tit.setEnabled(true);
        } else if (tn.equals(ToolBarDes.TN_PREFERENCE)) {
            tit.setEnabled(true);
        } else if (tn.equals(ToolBarDes.TN_HELP)) {
            tit.setEnabled(true);
        } else {
            tit.setEnabled(false);
        }
    }
    void updateToolBars(SlogTabFrame it) {
        for (ToolItem tit : mToolItems) {
            if (it == null) {
                updateToolItem(tit);
            } else {
                it.updateToolItem(tit);
            }
        }
        mCoolBar.update();
    }
    protected Control createContents() {

        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        getShell().setLayout(layout);
        createToolBars();
        updateToolBars(null);
        mTabFolder = new CTabFolder(getShell(), SWT.BORDER);
        mTabFolder.setSimple(false);
        mTabFolder.setUnselectedCloseVisible(true);
        mTabFolder.setUnselectedImageVisible(true);
        
        mTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        /*
        mTabFolder.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                  if (e.item instanceof SlogTabFrame) {
                      SlogTabFrame it = (SlogTabFrame)e.item;
//                      updateToolBars(it);
                  }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if (e.item instanceof SlogTabFrame) {
                    SlogTabFrame it = (SlogTabFrame)e.item;
  //                  updateToolBars(it);
                }
            }
           
        });
        */
        mTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            public void close(CTabFolderEvent event) {
                if (event.item instanceof SlogTabFrame) {
                    SlogTabFrame it =  (SlogTabFrame)event.item;
                    it.onClose();
                    if (mTabFolder.getItemCount() == 1) {
                        updateToolBars(null);
                    }
                }
            }
        });
        
        mTabFolder.setFocus();
        getDisplay().addFilter(SWT.KeyDown, new Listener() {
            @Override
            public void handleEvent(Event e) {
                
                for (ToolItem it : mToolItems) {
                    if (it.isEnabled()) {
                        Integer key = (Integer) it.getData("KeyAccelerator");
                        if (key.intValue() == (e.stateMask|e.keyCode)) {
                            it.notifyListeners(SWT.Selection, null);
                        }
                        
                    }
                }
                /*
                if(((e.stateMask & SWT.COMMAND) == SWT.COMMAND) && (e.keyCode == 'f')) {
                    ToolItem it =  mToolItems.get(ToolBarDes.TN_SEARCH);
                    if (it.isEnabled()) {
                        mToolItems.get(ToolBarDes.TN_SEARCH).notifyListeners(SWT.Selection, null);
                    }
                }
                */
            }});
        
        getShell().setSize(1200, 800);
     
        return getShell();

    }

    public void closeTabFrames() {
        for(CTabItem it : mTabFolder.getItems()) {
            SlogTabFrame tbf = (SlogTabFrame) it;
            tbf.onClose();
        }
    }
    public void run() {
        mShell.open();
        Display display = getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
