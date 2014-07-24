package visit.java.client;

/**
 * Java classes to implement 3D transformation matrices.
 * 
 * You have my permission to use freely, as long as you keep the attribution. -
 * Ken Perlin
 * 
 * Why does this class exist? I created this class to support general purpose 3D
 * transformations. I use it in a number of the demos that run on my Web page.
 * 
 * What does the class do? You can use it to create 3D points and homogeneous
 * vectors, and also to create transformation matrices with these. There are
 * methods to rotate, translate, and scale transformations, and to apply
 * transformations to vectors. You can also get and set the elements of matrices
 * and vectors.
 * 
 * The classes Vector3D and Matrix3D are extended from respective generic
 * classes VectorN and MatrixN, which do most of the bookkeeping for arithmetic
 * vectors of length N and square matrices of size N x N, respectively.
 * 
 * @author hkq, tnp
 * 
 */

public class Transformation {

	/**
	 * Homogeneous transformation matrices in three dimensions
	 * 
	 * @author hkq, tnp (Credit: Kevin Perlin)
	 */
	public static class Matrix3D extends MatrixN {

		/**
		 * Create a new identity transformation
		 */
		public Matrix3D() {

			super(4);
			identity();

			return;
		}

		/**
		 * Rotate transformation about the X axis
		 * 
		 * @param theta
		 */
		public void rotateX(double theta) {

			Matrix3D tmp = new Matrix3D();
			double c = Math.cos(theta);
			double s = Math.sin(theta);

			tmp.set(1, 1, c);
			tmp.set(1, 2, -s);
			tmp.set(2, 1, s);
			tmp.set(2, 2, c);

			preMultiply(tmp);

			return;
		}

		/**
		 * Rotate transformation about the Y axis
		 * 
		 * @param theta
		 */
		public void rotateY(double theta) {

			Matrix3D tmp = new Matrix3D();
			double c = Math.cos(theta);
			double s = Math.sin(theta);

			tmp.set(2, 2, c);
			tmp.set(2, 0, -s);
			tmp.set(0, 2, s);
			tmp.set(0, 0, c);

			preMultiply(tmp);

			return;
		}

		/**
		 * Rotate transformation about the Z axis
		 * 
		 * @param theta
		 */
		public void rotateZ(double theta) {

			Matrix3D tmp = new Matrix3D();
			double c = Math.cos(theta);
			double s = Math.sin(theta);

			tmp.set(0, 0, c);
			tmp.set(0, 1, -s);
			tmp.set(1, 0, s);
			tmp.set(1, 1, c);

			preMultiply(tmp);

			return;
		}

		/**
		 * 
		 * @param theta
		 * @param vec
		 */
		public void rotate(double theta, Vector3D vec) {

			double ux = vec.get(0);
			double uy = vec.get(1);
			double uz = vec.get(2);
			
			double sin_theta = Math.sin(theta);
			double cos_theta = Math.cos(theta);
			double one_minus_cos_theta = (1 - Math.cos(theta));

			double m00 = Math.cos(theta) + (ux * ux * one_minus_cos_theta);
			double m01 = (ux * uy * one_minus_cos_theta) - (uz * sin_theta);
			double m02 = (ux * uz * one_minus_cos_theta) - (uy * sin_theta);

			double m10 = (uy * ux * one_minus_cos_theta) + (uz * sin_theta);
			double m11 = (cos_theta) + (uy * uy * one_minus_cos_theta);
			double m12 = (uy * uz * one_minus_cos_theta) - (ux * sin_theta);

			double m20 = (uz * ux * one_minus_cos_theta) - (uy * sin_theta);
			double m21 = (uz * uy * one_minus_cos_theta) + (uz * sin_theta);
			double m22 = (cos_theta) + (uz * uz * one_minus_cos_theta);

			set(0, 0, m00);
			set(0, 1, m01);
			set(0, 2, m02);

			set(1, 0, m10);
			set(1, 1, m11);
			set(1, 2, m12);

			set(2, 0, m20);
			set(2, 1, m21);
			set(2, 2, m22);

			return;
		}

		/**
		 * 
		 * @param theta
		 * @param vec
		 * @return
		 */
		public Matrix3D rotated(double theta, Vector3D vec) {

			Matrix3D result = new Matrix3D();
			result.set(this);
			result.rotate(theta, vec);

			return result;
		}

		/**
		 * 
		 * @param a
		 * @param b
		 * @param c
		 */
		public void translate(double a, double b, double c) { // <b>translate</b>

			Matrix3D tmp = new Matrix3D();

			tmp.set(0, 3, a);
			tmp.set(1, 3, b);
			tmp.set(2, 3, c);

			preMultiply(tmp);

			return;
		}

		/**
		 * 
		 * @param v
		 */
		public void translate(Vector3D v) {

			translate(v.get(0), v.get(1), v.get(2));

			return;
		}

		/**
		 * Scale uniformly
		 * 
		 * @param s
		 */
		public void scale(double s) {

			Matrix3D tmp = new Matrix3D();

			tmp.set(0, 0, s);
			tmp.set(1, 1, s);
			tmp.set(2, 2, s);

			preMultiply(tmp);

			return;
		}

		/**
		 * Scale non-uniformly
		 * 
		 * @param r
		 * @param s
		 * @param t
		 */
		public void scale(double r, double s, double t) {

			Matrix3D tmp = new Matrix3D();

			tmp.set(0, 0, r);
			tmp.set(1, 1, s);
			tmp.set(2, 2, t);

			preMultiply(tmp);

			return;
		}

		/**
		 * 
		 * @param v
		 */
		public void scale(Vector3D v) {

			scale(v.get(0), v.get(1), v.get(2));

			return;
		}
	}

	/**
	 * Homogeneous vectors in three dimensions
	 * 
	 * @author hkq, tnp (Credit: Kevin Perlin)
	 */
	public static class Vector3D extends VectorN {

		/**
		 * Create a new 3D homogeneous vector
		 */
		public Vector3D() {

			super(4);

			return;
		}

		/**
		 * Set the value of vector
		 * 
		 * @param x
		 * @param y
		 * @param z
		 * @param w
		 */
		public void set(double x, double y, double z, double w) {

			set(0, x);
			set(1, y);
			set(2, z);
			set(3, w);

			return;
		}

		/**
		 * Set the value of a 3D point
		 * 
		 * @param x
		 * @param y
		 * @param z
		 */
		public void set(double x, double y, double z) {

			set(x, y, z, 1);

			return;
		}
		
		public Vector3D normalized() {

			Vector3D n = new Vector3D();
			n.set(this);
			n.normalize();

			return n;
		}

		/**
		 * 
		 * @param B
		 * @return
		 */
		public Vector3D cross(Vector3D B) {

			Vector3D result = new Vector3D();

			// (A[1] * B[2]) - (A[2] * B[1]),
			// (A[2] * B[0]) - (A[0] * B[2]),
			// (A[0] * B[1]) - (A[1] * B[0])
			result.set((get(1) * B.get(2)) - (get(2) * B.get(1)),
					(get(2) * B.get(0)) - (get(0) * B.get(2)),
					(get(0) * B.get(1)) - (get(1) * B.get(0)));

			return result;
		}
	}

	/**
	 * Geometric vectors of size N
	 * 
	 * @author hkq, tnp (Credit: Kevin Perlin)
	 */
	public static class VectorN {

		/**
		 * 
		 */
		private double v[];

		/**
		 * Create a new vector
		 * 
		 * @param n
		 */
		public VectorN(int n) {

			v = new double[n];

			return;
		}

		/**
		 * Return vector size
		 * 
		 * @return
		 */
		public int size() {

			return v.length;

		}

		/**
		 * Get one element
		 * 
		 * @param j
		 * @return
		 */
		public double get(int j) {

			return v[j];

		}

		/**
		 * Set one element
		 * 
		 * @param j
		 * @param f
		 */
		public void set(int j, double f) {

			v[j] = f;

		}

		/**
		 * Copy from another vector
		 * 
		 * @param vec
		 */
		public void set(VectorN vec) {

			for (int j = 0; j < size(); j++)
				set(j, vec.get(j));

		}

		/**
		 * Convert to string representation
		 */
		public String toString() {

			String s = "{";
			for (int j = 0; j < size(); j++)
				s += (j == 0 ? "" : ",") + get(j);

			return s + "}";
		}

		/**
		 * Multiply by an N x N matrix
		 * 
		 * @param mat
		 */
		public void transform(MatrixN mat) {

			VectorN tmp = new VectorN(size());
			double f;

			for (int i = 0; i < size(); i++) {
				f = 0.;
				for (int j = 0; j < size(); j++)
					f += mat.get(i, j) * get(j);
				tmp.set(i, f);
			}
			set(tmp);

			return;
		}

		/**
		 * Euclidean distance
		 * 
		 * @return
		 */
		public double length() {

			double d = 0;
			for (int i = 0; i < size(); i++) {
				d += get(i) * get(i);
			}

			return Math.sqrt(d);
		}

		/**
		 * 
		 */
		public void normalize() {

			double d = length();

			if (d == 0.0)
				d = 1.0;

			for (int i = 0; i < size(); i++) {
				v[i] /= d;
			}

			return;
		}

		/**
		 * 
		 * @return
		 */
		public VectorN normalized() {

			VectorN n = new VectorN(this.size());
			n.set(this);
			n.normalize();

			return n;
		}

		/**
		 * Euclidean distance
		 * 
		 * @param vec
		 * @return
		 */
		public double distance(VectorN vec) {

			double x, y, d = 0;

			for (int i = 0; i < size(); i++) {
				x = vec.get(0) - get(0);
				y = vec.get(1) - get(1);
				d += x * x + y * y;
			}

			return Math.sqrt(d);
		}

		/**
		 * 
		 * @param vec
		 * @return
		 */
		public double dot(VectorN vec) {

			double product = 0;

			if (size() != vec.size()) {
				// / TODO throw error
				System.err.println("vec size mismatch");
				return 0;
			}

			for (int i = 0; i < size(); ++i) {
				product += get(i) * vec.get(i);
			}

			return product;
		}
	}

	/**
	 * Geometric matrices of size N x N
	 * 
	 * @author tnp (Credit: Kevin Perlin)
	 */
	public static class MatrixN {

		/**
		 * 
		 */
		private VectorN v[];

		/**
		 * Make a new square matrix
		 * 
		 * @param n
		 */
		public MatrixN(int n) {

			v = new VectorN[n];
			for (int i = 0; i < n; i++)
				v[i] = new VectorN(n);

			return;
		}

		/**
		 * 
		 * @return Number of rows
		 */
		public int size() {
			return v.length;
		}

		/**
		 * Get one element
		 * 
		 * @param i
		 * @param j
		 * @return
		 */
		public double get(int i, int j) {
			return get(i).get(j);
		}

		/**
		 * Set one element
		 * 
		 * @param i
		 * @param j
		 * @param f
		 */
		public void set(int i, int j, double f) {

			v[i].set(j, f);

			return;
		}

		/**
		 * Get one row
		 * 
		 * @param i
		 * @return
		 */
		public VectorN get(int i) {
			return v[i];
		}

		/**
		 * Set one row
		 * 
		 * @param i
		 * @param vec
		 */
		public void set(int i, VectorN vec) {

			v[i].set(vec);

			return;
		}

		/**
		 * Copy contents of another matrix
		 * 
		 * @param mat
		 */
		public void set(MatrixN mat) {

			for (int i = 0; i < size(); i++)
				set(i, mat.get(i));

			return;
		}

		/**
		 * Convert to string representation
		 * 
		 * @return
		 */
		public String toString() {

			String s = "{";

			for (int i = 0; i < size(); i++)
				s += (i == 0 ? "" : ",") + get(i);

			return s + "}";
		}

		/**
		 * Set to the identity matrix
		 */
		public void identity() {

			for (int j = 0; j < size(); j++)
				for (int i = 0; i < size(); i++)
					set(i, j, (i == j ? 1 : 0));

			return;
		}

		/**
		 * 
		 * @param mat
		 */
		public void preMultiply(MatrixN mat) {

			MatrixN tmp = new MatrixN(size());
			double f;

			for (int j = 0; j < size(); j++)
				for (int i = 0; i < size(); i++) {
					f = 0.;
					for (int k = 0; k < size(); k++)
						f += mat.get(i, k) * get(k, j);
					tmp.set(i, j, f);
				}

			set(tmp);

			return;
		}

		/**
		 * 
		 * @param mat
		 */
		public void postMultiply(MatrixN mat) {

			MatrixN tmp = new MatrixN(size());
			double f;

			for (int j = 0; j < size(); j++)
				for (int i = 0; i < size(); i++) {
					f = 0.;
					for (int k = 0; k < size(); k++)
						f += get(i, k) * mat.get(k, j);
					tmp.set(i, j, f);
				}

			set(tmp);

			return;
		}

		/**
		 * 
		 */
		public void transpose() {

			for (int i = 0; i < size() - 2; ++i) {
				for (int j = i + 1; j < size() - 1; ++j) {
					double swap = get(i, j);
					set(i, j, get(j, i));
					set(j, i, swap);
				}
			}
		}
		
		public MatrixN transposed() {

			MatrixN tmp = new MatrixN(size());
			tmp.set(this);
			tmp.transpose();
			return tmp;
		}
	}
};
