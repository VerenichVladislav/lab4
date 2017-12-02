package Lab4;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JPanel;

import static java.lang.Math.abs;

public class GraphicsDisplay extends JPanel {

    private ArrayList<Double[]> graphicsData;
    private int selectedMarker = -1;
    //координаты для приближения графика(выбора оптимального режима показа)
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double scaleX;
    private double scaleY;
    //массив для приближения
    private double[][] viewport = new double[2][2];
    /*
    булевские константы пригодятся для функций отображения
     */
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean clockRotate = false;
    private boolean antiClockRotate = false;
    /*
    Класс Font необходим для работы с шрифтами. У него есть полезные константы:
    BOLD - жирное начертание
    ITALIC - курсивное начертание
    PLAIN - обычное начертание
     */
    private Font axisFont;
    private Font labelsFont;

    /*
    Класс BasicStroke необоходим для работы с пером. У него есть очень полезные константы:
    CAP_ROUND - закругленный конец линии;
    CAP_SQUARE - квадратный конец линии;
    CAP_BUTT - оформление отсутствует;
    JOIN_ROUND - линии сопрягаются дугой окружности;
    JOIN_BEVEL - линии сопрягаются отрезком прямой, перпендикулярным биссектрисе угла между линиями;
    JOIN_MITER - линии просто стыкуются;
     */
    private BasicStroke axisStroke;
    private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;
    private BasicStroke gridStroke;
    private BasicStroke selectionStroke;

    /*
    DecimalFormat - класс, который помогает делать вывод данных красивым
     */
    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();

    private boolean scaleMode = false;

    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();
    /*
    В следующем методе я задал шрифты для всех необходимых компонент. Шрифты задаются при создании объекта
    класса BasicStroke (передаются в параметрах).
     */
    public GraphicsDisplay ()	{
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, new float[]{3,1,1,1,1,1,2,1,2,1}, 0.0f);
        //разные типы линий
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);//для осей
        markerStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, null, 0.0f);//для линии графика(обычный шрифт)
        selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[] { 10, 10 }, 0.0F);
        gridStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 5.0f, new float [] {5,5}, 2.0f);//для сетки
        //разные шрифты
        axisFont = new Font("Serif", Font.BOLD, 36);
        labelsFont = new java.awt.Font("Serif",0,10);

    }
    /*
    Данный метод позволяет подготовиться окно для отрисовки в нем графика. Верхний - нижний и правый - левый предел
    определяется максимумами Y и X. Этот метод это и сделает
     */
    public void showGraphics(ArrayList<Double[]> graphicsData)	{
        this.graphicsData = graphicsData;


        this.minX = (graphicsData.get(0))[0].doubleValue();
        this.maxX = (graphicsData.get(graphicsData.size() - 1))[0].doubleValue();
        this.minY = (graphicsData.get(0))[1].doubleValue();
        this.maxY = this.minY;

        for (int i = 1; i < graphicsData.size(); i++) {
            if ((graphicsData.get(i))[1].doubleValue() < this.minY) {
                this.minY = (graphicsData.get(i))[1].doubleValue();//находим минимум Y перебором
            }
            if ((graphicsData.get(i))[1].doubleValue() > this.maxY) {
                this.maxY = (graphicsData.get(i))[1].doubleValue();//находим макимум X перебором
            }
        }
        zoomToRegion(minX, maxY, maxX, minY);

    }
    /*
    Данный метод уменьшает картинку до нужных размеров(чтобы график был на весь экран).
     */
    public void zoomToRegion(double x1,double y1,double x2,double y2)	{
        this.viewport[0][0]=x1;//-2
        this.viewport[0][1]=y1;//12
        this.viewport[1][0]=x2;//2
        this.viewport[1][1]=y2;//3
        this.repaint();
    }
    /*
    Данный метод отображает оси.
     */
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }
    /*
    Данный метод отображает маркеры.
     */
    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }
    /*
    Данный метод делает смещение. Он помогает корректировать компоненты на выводе, чтоб они не
    накладывались друг на друга.
     */
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        // Инициализируем новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
        // Задаем еѐ координаты как координаты существующей точки +
        // заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
    /*
    Данный метод рисует и отображает сетку
     */
    protected void paintGrid (Graphics2D canvas) {
        canvas.setStroke(gridStroke);
        canvas.setColor(Color.GRAY);
        double pos = viewport[0][0];;
        double step = (viewport[1][0] - viewport[0][0])/10;

        while (pos < viewport[1][0]){
            canvas.draw(new Line2D.Double(xyToPoint(pos, viewport[0][1]), xyToPoint(pos, viewport[1][1])));
            pos += step;
        }
        canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0],viewport[0][1]), xyToPoint(viewport[1][0],viewport[1][1])));

        pos = viewport[1][1];
        step = (viewport[0][1] - viewport[1][1]) / 10;
        while (pos < viewport[0][1]){
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0], pos), xyToPoint(viewport[1][0], pos)));
            pos=pos + step;
        }
        canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0],viewport[0][1]), xyToPoint(viewport[1][0],viewport[0][1])));
    }
    /*
    Данный метод нарисует и отобразит линию графика.
     */
    protected void paintGraphics (Graphics2D canvas) {
        canvas.setStroke(this.graphicsStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        for (Double[] point : this.graphicsData)
        {
            if ((point[0].doubleValue() >= this.viewport[0][0]) && (point[1].doubleValue() <= this.viewport[0][1]) &&
                    (point[0].doubleValue() <= this.viewport[1][0]) && (point[1].doubleValue() >= this.viewport[1][1]))
            {
                if ((currentX != null) && (currentY != null)) {
                    canvas.draw(new Line2D.Double(xyToPoint(currentX.doubleValue(), currentY.doubleValue()),
                            xyToPoint(point[0].doubleValue(), point[1].doubleValue())));
                }
                currentX = point[0];
                currentY = point[1];
            }
        }


    }
    /*
    Данный метод нарисует и отобразит оси графика
     */
    protected void paintAxis(Graphics2D canvas){
        canvas.setStroke(this.axisStroke);
        canvas.setColor(java.awt.Color.BLACK);
        canvas.setFont(this.axisFont);
        FontRenderContext context=canvas.getFontRenderContext();
        if (!(viewport[0][0] > 0|| viewport[1][0] < 0)){
            canvas.draw(new Line2D.Double(xyToPoint(0, viewport[0][1]),
                    xyToPoint(0, viewport[1][1])));
            canvas.draw(new Line2D.Double(xyToPoint(-(viewport[1][0] - viewport[0][0]) * 0.0025,
                    viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),xyToPoint(0,viewport[0][1])));
            canvas.draw(new Line2D.Double(xyToPoint((viewport[1][0] - viewport[0][0]) * 0.0025,
                    viewport[0][1] - (viewport[0][1] - viewport[1][1]) * 0.015),
                    xyToPoint(0, viewport[0][1])));
            Rectangle2D bounds = axisFont.getStringBounds("y",context);
            Point2D.Double labelPos = xyToPoint(0.0, viewport[0][1]);
            canvas.drawString("y",(float)labelPos.x + 10,(float)(labelPos.y + bounds.getHeight() / 2));
        }
        if (!(viewport[1][1] > 0.0D || viewport[0][1] < 0.0D)){
            canvas.draw(new Line2D.Double(xyToPoint(viewport[0][0],0),
                    xyToPoint(viewport[1][0],0)));
            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0,
                    (viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
            canvas.draw(new Line2D.Double(xyToPoint(viewport[1][0] - (viewport[1][0] - viewport[0][0]) * 0.01,
                    -(viewport[0][1] - viewport[1][1]) * 0.005), xyToPoint(viewport[1][0], 0)));
            Rectangle2D bounds = axisFont.getStringBounds("x",context);
            Point2D.Double labelPos = xyToPoint(this.viewport[1][0],0.0D);
            canvas.drawString("x",(float)(labelPos.x - bounds.getWidth() - 10),(float)(labelPos.y - bounds.getHeight() / 2));
        }
    }
    /*
    Данный метод нарисует все маркеры
     */
    protected void paintMarkers(Graphics2D canvas) {
        for (Double[] point : graphicsData) {

            boolean temp = true;
            double znach = point[1];
            double cifr1 = znach % 10;
            znach /= 10;
            while (abs(znach) > 0) {
                double cifr2 = znach % 10;
                znach /= 10;
                if (cifr1 < cifr2) {
                    temp = false;
                    break;
                }

            }
            if (!temp) {
                // Выбираем красный цвета для контуров маркеров
                canvas.setColor(Color.RED);
                // Выбираем красный цвет для закрашивания маркеров внутри
                canvas.setPaint(Color.RED);
            } else {
                // Выбираем красный цвета для контуров маркеров
                canvas.setColor(Color.BLUE);
                // Выбираем красный цвет для закрашивания маркеров внутри
                canvas.setPaint(Color.BLUE);
            }
            canvas.setStroke(markerStroke);
            GeneralPath path = new GeneralPath();
            Point2D.Double center = xyToPoint(point[0], point[1]);
            canvas.draw(new Line2D.Double(shiftPoint(center, -8, 0), shiftPoint(center, 8, 0)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 0, 8), shiftPoint(center, 0, -8)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 8, 8), shiftPoint(center, -8, -8)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -8, 8), shiftPoint(center, 8, -8)));
            Point2D.Double corner = shiftPoint(center, 3, 3);
        }
    }




    /*
    В данном методе мы оформляем и выводим цифры, которые нужны для подписей значений
    около осей графика.
     */
    private void paintLabels(Graphics2D canvas){
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context=canvas.getFontRenderContext();
        double labelYPos;
        double labelXPos;
        if (!(viewport[1][1] >= 0 || viewport[0][1] <= 0))
            labelYPos = 0;
        else labelYPos = viewport[1][1];
        if (!(viewport[0][0] >= 0 || viewport[1][0] <= 0.0D))
            labelXPos=0;
        else labelXPos = viewport[0][0];
        double pos = viewport[0][0];
        double step = (viewport[1][0] - viewport[0][0]) / 10;
        while (pos < viewport[1][0]){
            java.awt.geom.Point2D.Double point = xyToPoint(pos,labelYPos);
            String label = formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label,context);
            canvas.drawString(label, (float)(point.getX() + 5), (float)(point.getY() - bounds.getHeight()));
            pos=pos + step;
        }
        pos = viewport[1][1];
        step = (viewport[0][1] - viewport[1][1]) / 10.0D;//шаг или величина разбиения(чем больше,тем больше меток)
        while (pos < viewport[0][1]){
            Point2D.Double point = xyToPoint(labelXPos,pos);
            String label=formatter.format(pos);
            Rectangle2D bounds = labelsFont.getStringBounds(label,context);
            canvas.drawString(label,(float)(point.getX() + 5),(float)(point.getY() - bounds.getHeight()));
            pos=pos + step;
        }
        if (selectedMarker >= 0)
        {
            Point2D.Double point = xyToPoint((graphicsData.get(selectedMarker))[0].doubleValue(),
                    (graphicsData.get(selectedMarker))[1].doubleValue());
            String label = "X=" + formatter.format((graphicsData.get(selectedMarker))[0]) +
                    ", Y=" + formatter.format((graphicsData.get(selectedMarker))[1]);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLACK);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }
    }

    /*
    Данный метод отображет все компоненты графика
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        scaleX=this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        scaleY=this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        if ((this.graphicsData == null) || (this.graphicsData.size() == 0)) return;

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Font oldFont = canvas.getFont();
        Paint oldPaint = canvas.getPaint();
        if (clockRotate) {
            AffineTransform at = AffineTransform.getRotateInstance(Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2);
            at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
                    (getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2));
            canvas.setTransform(at);

        }
        if (antiClockRotate) {
            AffineTransform at = AffineTransform.getRotateInstance(-Math.PI/2, getSize().getWidth()/2, getSize().getHeight()/2);
            at.concatenate(new AffineTransform(getSize().getHeight()/getSize().getWidth(), 0.0, 0.0, getSize().getWidth()/getSize().getHeight(),
                    (getSize().getWidth()-getSize().getHeight())/2, (getSize().getHeight()-getSize().getWidth())/2));
            canvas.setTransform(at);

        }
        paintGrid(canvas);
        if (showAxis)
        {paintAxis(canvas);
            paintLabels(canvas);
        }
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        paintSelection(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);

    }

    /*
    Последующие методы нужны для конкретизации отображения и установки необходимых параметров
    для дальнейшей корректной работы программы.
     */
    private void paintSelection(Graphics2D canvas) {
        if (!scaleMode) return;
        canvas.setStroke(selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(selectionRect);
    }

    public void setClockRotate(boolean clockRotate) {
        this.clockRotate = clockRotate;
        repaint();
    }

    public void setAntiClockRotate(boolean antiClockRotate) {
        this.antiClockRotate = antiClockRotate;
        repaint();
    }

    public boolean isAntiClockRotate() {
        return antiClockRotate;
    }

    public void saveToTextFile(File selectedFile)	{
        try{
            PrintStream out = new PrintStream(selectedFile);
            out.println("координатные точки в формате .txt");
            for (Double[] point : graphicsData){
                out.println(point[0] + " " + point[1]);
            }
            out.close();
        }catch (FileNotFoundException e){}
    }
}
