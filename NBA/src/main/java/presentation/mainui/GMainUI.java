package presentation.mainui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;


public class GMainUI extends JPanel implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final double PI_DIV_2 = Math.PI / 2.0;
	protected enum AutomatedAction {AUTO_TURN, AUTO_DROP_GO_BACK, AUTO_DROP_BUT_TURN}

	protected int rotationX;
	protected double nextPageAngle;
	protected double backPageAngle;

	protected Timer timer;
	protected AutomatedAction action;
	protected Point autoPoint;
	protected Point tmpPoint;

	protected int leftPageIndex;

	protected Image currentLeftImage;
	protected Image currentRightImage;
	protected Image nextLeftImage;
	protected Image nextRightImage;
	protected Image previousLeftImage;
	protected Image previousRightImage;

	protected String pageLocation;
	protected String pageName;
	protected String pageExtension;
	protected int nrOfPages;
	protected boolean leftPageTurn;
	protected int refreshSpeed;

	// used to store the bounds of the book
	protected Rectangle bookBounds;
	protected int pageWidth;

	protected int shadowWidth;
	protected boolean borderLinesVisible;

	// vars for optimization and reuse
	protected Color shadowDarkColor;
	protected Color shadowNeutralColor;
	protected GeneralPath clipPath;

	/* 
	 * Constructor
	 */
	public GMainUI() {
		super();

		refreshSpeed = 0;//刷新速度
		shadowWidth = 100;//阴影宽度
		bookBounds = new Rectangle();
		borderLinesVisible = false;//是否显示书的边框
		clipPath = new GeneralPath();
		shadowDarkColor = new Color(0,0,0,130);
		shadowNeutralColor = new Color(255,255,255,0);//设置颜色
		//this.setBackground(Color.LIGHT_GRAY);

		timer = new Timer(refreshSpeed, this);
		timer.stop();
		leftPageIndex = 1;

		setPages(null, null, null,
				13, 210, 342);
		setMargins(70, 80);
	}

	///////////////////
	// PAINT METHODS //
	///////////////////

	@Override
	public void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		setGraphicsHints(g2);

		// background
		//g.setColor(this.getBackground());
		//g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// page 1
		paintPage(g2, currentLeftImage, bookBounds.x, bookBounds.y, pageWidth, bookBounds.height, this, false);

		// page 2
		paintPage(g2, currentRightImage, bookBounds.x + pageWidth, bookBounds.y, pageWidth, bookBounds.height, this, true);

		if (leftPageTurn) {
			paintLeftPageSoftClipped(g2);
		} else {
			paintRightPageSoftClipped(g2);
		}
	}

	protected void paintLeftPage(final Graphics2D g2) {
		// translate to rotation point
		g2.translate(bookBounds.width + bookBounds.x - rotationX + bookBounds.x,
				bookBounds.y + bookBounds.height);

		// rotate over back of page A angle
		g2.rotate(-backPageAngle);

		// translate the amount already done
		final int done = bookBounds.width + bookBounds.x - rotationX;
		g2.translate(done - pageWidth, 0);

		// page 3 (= back of page 1)
		clipPath.reset();
		clipPath.moveTo(pageWidth, 0);
		clipPath.lineTo(pageWidth-done, 0);
		clipPath.lineTo(pageWidth,
				-1 * (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();
		final Shape s = g2.getClip();
		g2.clip(clipPath);
		paintPage(g2, previousRightImage, 0, 0 - bookBounds.height, pageWidth, bookBounds.height, this, false);
		g2.setClip(s);

		// translate back
		g2.translate(pageWidth - done, 0);

		// rotate back
		g2.rotate(backPageAngle);

		// translate back
		g2.translate(rotationX - bookBounds.width - bookBounds.x - bookBounds.x,
				-bookBounds.y - bookBounds.height);

		// page 4
		clipPath.reset();
		clipPath.moveTo(bookBounds.x,
				bookBounds.height + bookBounds.y);
		clipPath.lineTo(bookBounds.x + done,
				bookBounds.height + bookBounds.y);
		clipPath.lineTo(bookBounds.x,
				bookBounds.height + bookBounds.y - (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();
		g2.clip(clipPath);
		paintPage(g2, previousLeftImage, bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height, this, true);

		// add drop shadow
		final GradientPaint grad = new GradientPaint(bookBounds.x + done,
				bookBounds.height + bookBounds.y,
				shadowDarkColor,
				bookBounds.x + done - shadowWidth,
				bookBounds.height + bookBounds.y + (int)(Math.cos(PI_DIV_2 - nextPageAngle) * shadowWidth),
				shadowNeutralColor,
				false);
		g2.setPaint(grad);
		g2.fillRect(bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height);
	}

	protected void paintLeftPageSoftClipped(final Graphics2D g2) {

		// calculate amount done (traveled)
		final int done = bookBounds.width + bookBounds.x - rotationX;

		///////////////////////////////
		// page 3 (= back of page 1) //
		///////////////////////////////

		// init clip path
		clipPath.reset();
		clipPath.moveTo(pageWidth, 0);
		clipPath.lineTo(pageWidth-done, 0);
		clipPath.lineTo(pageWidth,
				-1 * (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();

		// init soft clip image
		final GraphicsConfiguration gc = g2.getDeviceConfiguration();
		BufferedImage img = gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D gImg = img.createGraphics();

		// translate to rotation point
		gImg.translate(bookBounds.width + bookBounds.x - rotationX + bookBounds.x,
				bookBounds.y + bookBounds.height);

		// rotate over back of page A angle
		gImg.rotate(-backPageAngle);

		// translate the amount already done
		gImg.translate(done - pageWidth, 0);

		// init area on which may be painted
		gImg.setComposite(AlphaComposite.Clear);
		gImg.fillRect(0, 0, this.getWidth(), this.getHeight());
		gImg.setComposite(AlphaComposite.Src);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.setColor(Color.WHITE);
		gImg.fill(clipPath);
		gImg.setColor(new Color(255,255,255,0));
		gImg.fillRect(0, 0 - bookBounds.height - bookBounds.height,
				pageWidth+10, bookBounds.height); 	// remove the top margin from allowed area
		gImg.setComposite(AlphaComposite.SrcAtop);

		// paint page
		paintPage(gImg, previousRightImage, 0, 0 - bookBounds.height, pageWidth, bookBounds.height, this, false);

		// translate back
		gImg.translate(pageWidth - done, 0);

		// rotate back
		gImg.rotate(backPageAngle);

		// translate back
		gImg.translate(rotationX - bookBounds.width - bookBounds.x - bookBounds.x,
				-bookBounds.y - bookBounds.height);

		gImg.dispose();
		g2.drawImage(img, 0, 0, null);

		////////////
		// page 4 //
		////////////

		// init clip path
		clipPath.reset();
		clipPath.moveTo(bookBounds.x,
				bookBounds.height + bookBounds.y);
		clipPath.lineTo(bookBounds.x + done,
				bookBounds.height + bookBounds.y);
		clipPath.lineTo(bookBounds.x,
				bookBounds.height + bookBounds.y - (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();

		// init soft clip image
		img = gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
		gImg = img.createGraphics();
		gImg.setComposite(AlphaComposite.Clear);
		gImg.fillRect(0, 0, this.getWidth(), this.getHeight());
		gImg.setComposite(AlphaComposite.Src);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.setColor(Color.WHITE);
		gImg.fill(clipPath);								// init area on which may be painted
		gImg.setColor(new Color(255,255,255,0));
		gImg.fillRect(0,0,this.getWidth(), bookBounds.y);	// remove the top margin from allowed area
		gImg.setComposite(AlphaComposite.SrcAtop);

		// paint page
		paintPage(gImg, previousLeftImage, bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height, this, true);

		// paint shadow
		final GradientPaint grad = new GradientPaint(bookBounds.x + done,
				bookBounds.height + bookBounds.y,
				shadowDarkColor,
				bookBounds.x + done - shadowWidth,
				bookBounds.height + bookBounds.y + (int)(Math.cos(PI_DIV_2 - nextPageAngle) * shadowWidth),
				shadowNeutralColor,
				false);
		gImg.setPaint(grad);
		gImg.fillRect(bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height);

		gImg.dispose();
		g2.drawImage(img, 0, 0, null);
	}

	protected void paintRightPage(final Graphics2D g2) {

		// translate to rotation point
		g2.translate(rotationX, bookBounds.y + bookBounds.height);

		// rotate over back of page A angle
		g2.rotate(backPageAngle);

		// translate the amount already done
		final int done = bookBounds.width + bookBounds.x - rotationX;
		g2.translate(-done, 0);

		// page 3 (= back of page 1)
		clipPath.reset();
		clipPath.moveTo(0, 0);
		clipPath.lineTo(done, 0);
		clipPath.lineTo(0,
				-1 * (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();
		final Shape s = g2.getClip();
		g2.clip(clipPath);
		paintPage(g2, nextLeftImage, 0, 0 - bookBounds.height, pageWidth, bookBounds.height, this, false);
		g2.setClip(s);

		// translate back
		g2.translate(done, 0);

		// rotate back
		g2.rotate(-backPageAngle);

		// translate back
		g2.translate(-rotationX, -bookBounds.y - bookBounds.height);

		// page 4
		clipPath.reset();
		clipPath.moveTo(bookBounds.width + bookBounds.x,
				bookBounds.height + bookBounds.y);
		clipPath.lineTo(rotationX, bookBounds.height + bookBounds.y);
		clipPath.lineTo(bookBounds.width + bookBounds.x,
				bookBounds.height + bookBounds.y - (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();
		g2.clip(clipPath);
		paintPage(g2, nextRightImage, pageWidth + bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height, this, true);

		// add drop shadow
		final GradientPaint grad = new GradientPaint(rotationX,
				bookBounds.height + bookBounds.y,
				shadowDarkColor,
				rotationX + shadowWidth,
				bookBounds.height + bookBounds.y + (int)(Math.cos(PI_DIV_2 - nextPageAngle) * shadowWidth),
				shadowNeutralColor,
				false);
		g2.setPaint(grad);
		g2.fillRect(pageWidth + bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height);
	}

	protected void paintRightPageSoftClipped(final Graphics2D g2) {

		final int done = bookBounds.width + bookBounds.x - rotationX;

		///////////////////////////////
		// page 3 (= back of page 1) //
		///////////////////////////////

		// init clip path
		clipPath.reset();
		clipPath.moveTo(0, 0);
		clipPath.lineTo(done, 0);
		clipPath.lineTo(0,
				-1 * (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();

		// init soft clip image
		final GraphicsConfiguration gc = g2.getDeviceConfiguration();
		BufferedImage img = gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D gImg = img.createGraphics();

		// translate to rotation point
		gImg.translate(rotationX, bookBounds.y + bookBounds.height);

		// rotate over back of page A angle
		gImg.rotate(backPageAngle);

		// translate the amount already done
		gImg.translate(-done, 0);

		// init area on which may be painted
		gImg.setComposite(AlphaComposite.Clear);
		gImg.fillRect(0, 0, this.getWidth(), this.getHeight());
		gImg.setComposite(AlphaComposite.Src);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.setColor(Color.WHITE);
		gImg.fill(clipPath);
		gImg.setColor(new Color(255,255,255,0));
		gImg.fillRect(-10, 0 - bookBounds.height - bookBounds.height,
				pageWidth+10, bookBounds.height); 	// remove the top margin from allowed area
		gImg.setComposite(AlphaComposite.SrcAtop);

		// paint page
		paintPage(gImg, nextLeftImage, 0, 0 - bookBounds.height, pageWidth, bookBounds.height, this, false);

		// translate back
		gImg.translate(done, 0);

		// rotate back
		gImg.rotate(-backPageAngle);

		// translate back
		gImg.translate(-rotationX, -bookBounds.y - bookBounds.height);

		gImg.dispose();
		g2.drawImage(img, 0, 0, null);

		////////////
		// page 4 //
		////////////

		// init clip path
		clipPath.reset();
		clipPath.moveTo(bookBounds.width + bookBounds.x,
				bookBounds.height + bookBounds.y);
		clipPath.lineTo(rotationX, bookBounds.height + bookBounds.y);
		clipPath.lineTo(bookBounds.width + bookBounds.x,
				bookBounds.height + bookBounds.y - (int)(Math.tan(PI_DIV_2 - nextPageAngle) * done));
		clipPath.closePath();

		// init soft clip image
		img = gc.createCompatibleImage(this.getWidth(), this.getHeight(), Transparency.TRANSLUCENT);
		gImg = img.createGraphics();
		gImg.setComposite(AlphaComposite.Clear);
		gImg.fillRect(0, 0, this.getWidth(), this.getHeight());
		gImg.setComposite(AlphaComposite.Src);
		gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gImg.setColor(Color.WHITE);
		gImg.fill(clipPath);								// init area on which may be painted
		gImg.setColor(new Color(255,255,255,0));
		gImg.fillRect(0,0,this.getWidth(), bookBounds.y);	// remove the top margin from allowed area
		gImg.setComposite(AlphaComposite.SrcAtop);

		// paint page
		paintPage(gImg, nextRightImage, pageWidth + bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height, this, true);

		// add drop shadow
		final GradientPaint grad = new GradientPaint(rotationX,
				bookBounds.height + bookBounds.y,
				shadowDarkColor,
				rotationX + shadowWidth,
				bookBounds.height + bookBounds.y + (int)(Math.cos(PI_DIV_2 - nextPageAngle) * shadowWidth),
				shadowNeutralColor,
				false);
		gImg.setPaint(grad);
		gImg.fillRect(pageWidth + bookBounds.x, bookBounds.y,
				pageWidth, bookBounds.height);

		gImg.dispose();
		g2.drawImage(img, 0, 0, null);
	}

	protected void paintPage(final Graphics2D g, final Image img, final int x, final int y, final int w, final int h, final ImageObserver o,
			final boolean rightPage) {
		if (img == null) {
			final Color oldColor = g.getColor();
			g.setColor(this.getBackground());
			g.fillRect(x, y, w+10, h+10);
			g.setColor(oldColor);
			return;
		}
		final Color oldColor = g.getColor();
		g.drawImage(img, x, y, w, h, o);

		// stop if no borders are needed
		if (!borderLinesVisible) {
			return;
		}

		if (rightPage) {
			g.setColor(Color.GRAY);
			g.drawLine(x + w, y,
					x + w, y + h);
			g.drawLine(x, y,
					x + w, y);
			g.drawLine(x, y + h,
					x + w, y + h);

			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(x + w - 1, y + 1,
					x + w - 1, y + h - 1);
			g.drawLine(x, y + 1,
					x + w - 1, y + 1);
			g.drawLine(x, y + h - 1,
					x + w - 1, y + h - 1);

			g.drawLine(x, y + 2, x, y + h - 2);

			g.setColor(oldColor);

		} else {
			g.setColor(Color.GRAY);
			g.drawLine(x, y,
					x, y + h);
			g.drawLine(x, y,
					x + w, y);
			g.drawLine(x, y + h,
					x + w, y + h);

			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(x + 1, y + 1,
					x + 1, y + h - 1);
			g.drawLine(x + 1, y + 1,
					x + w - 1, y + 1);
			g.drawLine(x + 1, y + h - 1,
					x + w - 1, y + h - 1);

			g.drawLine(x + w - 1, y + 2, x + w - 1, y + h - 2);

			g.setColor(oldColor);
		}
	}



	public void actionPerformed(final ActionEvent e) {

		switch (action) {

		case AUTO_TURN:

			// update autoPoint
			double nextX = autoPoint.getX() - 15.0;
			double x = nextX;
			x = x - bookBounds.x - pageWidth;
			double y = -1.0 * Math.pow(x / pageWidth, 2);
			y += 1;
			y *= 50;

			if (leftPageTurn) {
				nextX = this.transformIndex(nextX);
			}

			autoPoint.setLocation(nextX, bookBounds.y + bookBounds.height - y);

			if (nextX <= bookBounds.x || nextX >= bookBounds.x + bookBounds.width) {
				timer.stop();
				action = null;
				switchImages();
				autoPoint.x = bookBounds.x;
				calculate(autoPoint);
				initRotationX();
				this.repaint();
				return;
			}

			// calculate using new point
			calculate(autoPoint);

			break;

		case AUTO_DROP_GO_BACK:

			final int xDiff = bookBounds.width + bookBounds.x - autoPoint.x;
			int stepsNeeded = 1 + (xDiff / 15);
			if (stepsNeeded == 0) {
				stepsNeeded = 1;
			}
			final int yStep = (bookBounds.y + bookBounds.height - autoPoint.y) / stepsNeeded;

			tmpPoint.x = tmpPoint.x + 15;
			tmpPoint.y = tmpPoint.y + yStep;
			if (tmpPoint.x >= bookBounds.width + bookBounds.x) {
				timer.stop();
				action = null;
			}

			final Point newPoint = (Point) tmpPoint.clone();

			if (leftPageTurn) {
				newPoint.x = this.transformIndex(tmpPoint.x);
			}

			// calculate using new point
			calculate(newPoint);

			break;

		case AUTO_DROP_BUT_TURN:

			final int xDiff2 = autoPoint.x - bookBounds.x;
			int stepsNeeded2 = 1 + (xDiff2 / 15);
			if (stepsNeeded2 == 0) {
				stepsNeeded2 = 1;
			}
			final int yStep2 = (bookBounds.y + bookBounds.height - autoPoint.y) / stepsNeeded2;

			tmpPoint.x = tmpPoint.x - 15;
			tmpPoint.y = tmpPoint.y + yStep2;
			if (tmpPoint.x <= bookBounds.x) {
				timer.stop();
				action = null;
				switchImages();
				if (leftPageTurn) {
					tmpPoint.x = this.transformIndex(bookBounds.x);
				} else {
					tmpPoint.x = bookBounds.x;
				}
				tmpPoint.y = bookBounds.y + bookBounds.height;
				calculate(tmpPoint);
				initRotationX();
				this.repaint();
				return;
			}

			final Point newPoint2 = (Point) tmpPoint.clone();

			if (leftPageTurn) {
				newPoint2.x = this.transformIndex(tmpPoint.x);
			}

			// calculate using new point
			calculate(newPoint2);

			break;
		}
	}

	////////////////////
	// HELPER METHODS //
	////////////////////

	protected double transformIndex(final double x) {
		return bookBounds.width + bookBounds.x - x + bookBounds.x;
	}

	protected int transformIndex(final int x) {
		return bookBounds.width + bookBounds.x - x + bookBounds.x;
	}

	protected void calculate(final Point p) {

		if (leftPageTurn) {
			p.x = transformIndex(p.x);
		}

		// if no page to turn available then dont
		// allow turn effect
		if (currentRightImage == null && !leftPageTurn) {
			rotationX = bookBounds.width + bookBounds.x;
			nextPageAngle = 0;
			backPageAngle = 0;
			return;
		} else if (currentLeftImage == null && leftPageTurn) {
			rotationX = bookBounds.width + bookBounds.x;
			nextPageAngle = 0;
			backPageAngle = 0;
			return;
		}

		final Point cp = new Point(bookBounds.width + bookBounds.x, bookBounds.y + bookBounds.height);
		double bRico = 1.0 * (cp.x - p.getX()) / (cp.y - p.getY());
		bRico = -bRico;
		final Point mid = new Point(0,0);
		mid.x = (int)((cp.x + p.getX()) / 2.0);
		mid.y = (int)((cp.y + p.getY()) / 2.0);
		final double c = mid.y - bRico * mid.x;
		rotationX = Math.max((int)((cp.y - c)/ bRico), pageWidth + bookBounds.x);

		nextPageAngle = PI_DIV_2 - Math.abs(Math.atan(bRico));
		backPageAngle = 2.0 * nextPageAngle;
		this.repaint();
	}

	public void nextPage() {
		action = AutomatedAction.AUTO_TURN;
		autoPoint = new Point(bookBounds.x + bookBounds.width,
				bookBounds.y + bookBounds.height);
		this.timer.restart();
	}

	public void previousPage() {
		leftPageTurn = true;
		nextPage();
	}

	protected void initRotationX() {
		rotationX = bookBounds.width + bookBounds.x;
	}

	protected boolean passedThreshold() {
		final int done = bookBounds.width + bookBounds.x - rotationX;
		double X = Math.min(Math.tan(PI_DIV_2 - nextPageAngle) * done, bookBounds.height);
		X *= done * 2;
		final double threshold = bookBounds.height * pageWidth;
		return X > threshold;
	}

	protected void switchImages() {
		if (leftPageTurn) {
			leftPageIndex -= 2;
			currentLeftImage = previousLeftImage;
			currentRightImage = previousRightImage;

		} else {
			leftPageIndex += 2;
			currentLeftImage = nextLeftImage;
			currentRightImage = nextRightImage;
		}

		nextLeftImage = getPage(leftPageIndex + 2);
		nextRightImage = getPage(leftPageIndex + 3);
		previousLeftImage = getPage(leftPageIndex - 2);
		previousRightImage = getPage(leftPageIndex - 1);
	}

	protected BufferedImage getBlankPage(final int index) {
		final BufferedImage img = new BufferedImage(pageWidth, bookBounds.height, BufferedImage.TYPE_3BYTE_BGR);
		final Graphics gfx = img.getGraphics();
		gfx.setColor(null);
		gfx.fillRect(0, 0, img.getWidth(), img.getHeight());
		return img;
	}

	protected void setGraphicsHints(final Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
	}

	protected Image loadPage(final int index) {
		return new ImageIcon(pageLocation + pageName
				+ index + "." + pageExtension).getImage();
	}

	protected Image getPage(final int index) {

		// if request goes beyond available pages return null
		if (index > nrOfPages) {

			// if back of existing page then return a blank
			if ( (index - 1) % 2 == 0) {
				return getBlankPage(index);
			} else {
				return null;
			}
		}
		if (index < 1) {
			if (index == 0) {
				try {
					return loadPage(index);
				} catch (final Exception e) {
					return getBlankPage(index);
				}
			} else {
				return null;
			}
		}

		// if no images specified return blank ones
		if (pageLocation == null
				|| pageName == null
				||  pageExtension == null ) {

			// create the blank page
			final BufferedImage img = getBlankPage(index);

			// draw page number
			final Graphics gfx = img.getGraphics();
			final Graphics2D g2 = (Graphics2D) gfx;
			setGraphicsHints(g2);

			return img;

		} else {
			return loadPage(index);
		}
	}

	/////////////////////////
	// GETTERS AND SETTERS //
	/////////////////////////

	public void setMargins(final int left, final int top) {
		bookBounds.x = left;
		bookBounds.y = top;

		initRotationX();
	}

	/*
	 * 设置图片
	 * 初始的第一张图片名，初始的第二张图片名，图片格式，图片数量，图片宽，图片高
	 */
	public void setPages(final String pageLocation, final String pageName, final String pageExtension, final int nrOfPages,
			final int pageWidth, final int pageHeight) {

		this.pageLocation = pageLocation;
		this.pageName = pageName;
		this.pageExtension = pageExtension;
		this.nrOfPages = nrOfPages;

		this.pageWidth = pageWidth;
		bookBounds.width = 2 * pageWidth;
		bookBounds.height = pageHeight;

		initRotationX();

		currentLeftImage = getPage(leftPageIndex);
		currentRightImage = getPage(leftPageIndex + 1);
		nextLeftImage = getPage(leftPageIndex + 2);
		nextRightImage = getPage(leftPageIndex + 3);
		previousLeftImage = getPage(leftPageIndex - 2);
		previousRightImage = getPage(leftPageIndex - 1);
	}

	public int getRefreshSpeed() {
		return refreshSpeed;
	}

	public void setRefreshSpeed(final int refreshSpeed) {
		this.refreshSpeed = refreshSpeed;
	}

	public Color getShadowDarkColor() {
		return shadowDarkColor;
	}

	public void setShadowDarkColor(final Color shadowDarkColor) {
		this.shadowDarkColor = shadowDarkColor;
	}

	public Color getShadowNeutralColor() {
		return shadowNeutralColor;
	}

	public void setShadowNeutralColor(final Color shadowNeutralColor) {
		this.shadowNeutralColor = shadowNeutralColor;
	}

	public int getShadowWidth() {
		return shadowWidth;
	}

	public void setShadowWidth(final int shadowWidth) {
		this.shadowWidth = shadowWidth;
	}

	public boolean isBorderLinesVisible() {
		return borderLinesVisible;
	}

	public void setBorderLinesVisible(final boolean borderLinesVisible) {
		this.borderLinesVisible = borderLinesVisible;
	}

	public int getLeftPageIndex() {
		return leftPageIndex;
	}

	public void setLeftPageIndex(int leftPageIndex) {
		if (leftPageIndex <= -1) {
			leftPageIndex = -1;
		}
		this.leftPageIndex = leftPageIndex;

		previousLeftImage = getPage(leftPageIndex - 2);
		previousRightImage = getPage(leftPageIndex - 1);
		currentLeftImage = getPage(leftPageIndex);;
		currentRightImage = getPage(leftPageIndex + 1);;
		nextLeftImage = getPage(leftPageIndex + 2);
		nextRightImage = getPage(leftPageIndex + 3);
	}

}
