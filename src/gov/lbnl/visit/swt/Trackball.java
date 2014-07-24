/**
 * 
 */
package gov.lbnl.visit.swt;

import visit.java.client.Transformation;

/**
 * @author hari
 *
 */
public class Trackball {
	/**
	 * 
	 */
	int screenWidth = 200;

	/**
	 * 
	 */
	int screenHeight = 200;

	/**
	 * 
	 */
	int radius = 50;

	/**
	 * 
	 */
	boolean moving = false;

	/**
	 * 
	 */
	Transformation.Vector3D p1;

	/**
	 * 
	 */
	Transformation.Matrix3D modelMat, oldModelMat;

	/**
	 * 
	 */
	Trackball() {
		p1 = new Transformation.Vector3D();
		modelMat = new Transformation.Matrix3D();
		oldModelMat = new Transformation.Matrix3D();
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void moveStart(int x, int y) {

		double xp = ((double) x - ((double) screenWidth * .5))
				/ (double) radius;
		double yp = ((double) y - ((double) screenHeight * .5))
				/ (double) radius;
		yp = -yp;
		double z = Math.sqrt(Math.max(1 - xp * xp - yp * yp, 0.0));

		// System.out.println(xp + " " + yp + " " + z);
		p1.set(xp, yp, z);
		moving = true;

		return;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void move(int x, int y) {

		if (!moving)
			return;

		double xp = ((double) x - ((double) screenWidth * .5))
				/ (double) radius;
		double yp = ((double) y - ((double) screenHeight * .5))
				/ (double) radius;

		yp = -yp;
		double z = Math.sqrt(Math.max(1 - xp * xp - yp * yp, 0));

		// System.out.println(x + " " + y + " " + screenWidth + " " +
		// screenHeight + " " + radius + " " + xp + " " + yp + " " + z);
		Transformation.Vector3D p2 = new Transformation.Vector3D();
		p2.set(xp, yp, z);

		Transformation.Vector3D n = p1.cross(p2);
		double theta = p1.normalized().dot(p2.normalized());

		Transformation.Matrix3D mat = new Transformation.Matrix3D();
		mat.rotate(theta, n.normalized());
		mat.preMultiply(oldModelMat);
		modelMat.set(mat);

		// System.out.println("--> " + theta + " " + n);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void moveStop(int x, int y) {
		oldModelMat.set(modelMat);
		moving = false;
	}

	/**
	 * Set the model matrices to identity
	 */
	public void resetModelMatrices() {
		oldModelMat.identity();
		modelMat.identity();
	}

	/**
	 * 
	 */
	public void wheelIn() {
		modelMat.scale(1.5);
	}

	/**
	 * 
	 */
	public void wheelOut() {
		modelMat.scale(0.5);
	}
}
