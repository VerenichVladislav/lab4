package Lab4;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.io.*;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;


public class MainFrame extends JFrame {

    private final int HEIGHT = 600;
    private final int WIDTH = 600;
    private boolean fileLoaded = false;
    /*
    GraphicsDisplay - класс, который необходим для рисования. С его помощью и еще с помощью класса
    Graphics2D мы можем рисовать все, что захотим.На элементах управления, например, JFrame, JPanel,
    JButton и других, есть возможность рисовать.
    Такие элементы обладают графическим контекстом, в этом контескте мы и рисуем.
    Всё, что нарисуем в контексте будет показано на элементе.
     */
    private GraphicsDisplay display = new GraphicsDisplay();
    /*
    JCheckBoxMenuItem - класс, который позволяет установить статус выбора команды меню.
    Может быть как выбран, так и не выбран. (галочка возле пункта меню)
     */
    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    /*
    JMenuItem - нужен для создания меню. С помощью методов этого класса (а их очень много) мы можем
    создать, отредактировать и подстроить под нас корректное меню программы.
     */
    private JMenuItem shapeRotateAntiClockItem;
    private JMenuItem saveToTextMenuItem;
    /*
    JFileChooser - класс, кторый только предоставляет возможность выбора файла или директории,
    больше ничего с ними не делает.
    С его помощью мы можем выбрать любой файл с компьютера.
     */
    private JFileChooser fileChooser = null;
    public MainFrame()  {
        super("Построение графиков функций");
        /*
        Далее мы создаем стандартную локацию (как и во второй лабе)
        и делаем основные компоненты меню.
         */
        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);
        //setExtendedState(MAXIMIZED_BOTH);//развертывание во весь экран

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл") {

            public void actionPerformed(ActionEvent arg0) {
                if (fileChooser==null) {
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("src."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION);
                openGraphics(fileChooser.getSelectedFile());
            }
        };
        fileMenu.add(openGraphicsAction);

        Action saveToTextAction = new AbstractAction("Сохранить  в .txt") {

            public void actionPerformed(ActionEvent arg0) {
                if (fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION){
                    display.saveToTextFile(fileChooser.getSelectedFile());
                }
            }
        };
        saveToTextMenuItem = fileMenu.add(saveToTextAction);

        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);

        Action rotatesShapeAntiClockAction = new AbstractAction("Поворот графика на 90 градусов") {
            public void actionPerformed(ActionEvent e) {
                if(display.isAntiClockRotate())
                {
                    display.setClockRotate(false);
                    display.setAntiClockRotate(false);
                }
                else
                    display.setAntiClockRotate(true);
            }
        };
        shapeRotateAntiClockItem = new JCheckBoxMenuItem(rotatesShapeAntiClockAction);
        graphicsMenu.add(shapeRotateAntiClockItem);
        shapeRotateAntiClockItem.setEnabled(false);
        graphicsMenu.addSeparator();

        Action showAxisAction = new AbstractAction("Показывать оси координат") {
            public void actionPerformed(ActionEvent e) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);

        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent e) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);

        graphicsMenu.addMenuListener((MenuListener) new GraphicsMenuListener());
        getContentPane().add(display, BorderLayout.CENTER);
    }

    /*
    Метод, который позволяет подгрузить нужный нам файл, а , если возникнут ошибки,
    то сообщить об этом в диалоговом окне.
     */
    protected void openGraphics(File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            ArrayList graphicsData = new ArrayList(50);
            while (in.available() > 0) {//больше 0 всегда
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData.add(new Double[] { x, y });
            }
            if (graphicsData.size() > 0) {
                fileLoaded = true;

                display.showGraphics(graphicsData);
            }
            in.close();
        }catch (FileNotFoundException e){
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE); return;
        }catch (IOException e){
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла",
                    "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE); return;
        }
    }

    private class GraphicsMenuListener implements MenuListener {

        /*
        Нам необходим только метод menuSelected, поэтому реализацию
        для других методов данного интерфейса мы можем не прописывать.
         */
        @Override
        public void menuCanceled(MenuEvent arg0) {

        }

        @Override
        public void menuDeselected(MenuEvent arg0) {

        }

        @Override
        public void menuSelected(MenuEvent arg0) {

            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            shapeRotateAntiClockItem.setEnabled(fileLoaded);
            saveToTextMenuItem.setEnabled(fileLoaded);
        }
    }

    /*
    В мэйне мы создаем фрэйм (а далее отработает его конструктор, который
    и решит нам всю задачу.)
     */
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
