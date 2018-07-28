import processing.core.PApplet;
import processing.event.MouseEvent;
import processing.event.KeyEvent;

import java.lang.Math;

public class ViewController3D
{
  class Position
  {
    double      r[];
    double      c[];
    double      d;
    
    Position() 
    {
      r = new double[]{ 0.0f, 0.0f, 0.0f, 1.0f };
      c = new double[]{ 0.0f, 0.0f, 0.0f };
      d = 1.0;
    }
    
    Position( Position p )
    {
      r = new double[]{ p.r[0], p.r[1], p.r[2], p.r[3] };
      c = new double[]{ p.c[0], p.c[1], p.c[2] };
      d = p.d;
    } 
  };

  enum Mode
  {
    NONE,
    ROTATE,
    ROTATE_Z,
    PAN,
    ZOOM,
    PUSH
  };

  PApplet  parent;
  
  Position       pCurr;
  Position       pHome;
  
  double         mOld[];
  double         mCur[];
  
  double         mvMatrix[];
  double         prMatrix[];
  
  Mode           m_mode;  
  
  // ---
  
  ViewController3D( PApplet parent )
  {
    this.parent = parent;
    this.parent.registerMethod( "mouseEvent", this );
    this.parent.registerMethod( "keyEvent", this );
    
    pCurr = new Position();

    pCurr.c[0] = 0.5;
    pCurr.c[1] = 0.5;
    pCurr.c[2] = 0.5;
    pCurr.d    = 2.0;
      
    pHome = new Position( pCurr );

    mOld = new double[2];
    mCur = new double[2];

    mvMatrix = new double[16];
    prMatrix = new double[16];
    
    update();
  }

  void begin( Mode mode, float mx, float my )
  {
    mOld[0] = mCur[0] = mx;
    mOld[1] = mCur[1] = my;

    m_mode = mode;        
  }
    
  void move( float mx, float my )
  {
    mCur[0] = mx;
    mCur[1] = my;

    switch( m_mode )
    {
    case ROTATE:
        apply_trackball( mOld, mCur );
        break;
    case ZOOM:
    {
        double s = 1.0 + (mCur[1] - mOld[1]);

        if( pCurr.d * s > 0.05 )
        {
            pCurr.d *= s;
        }
        else
        {
            double d[] = { 0, 0, -(mCur[1]-mOld[1])*s };

            rotate_vq( d, pCurr.r );
            pCurr.c[0] += d[0];
            pCurr.c[1] += d[1];
            pCurr.c[2] += d[2];
        }
        break;
    }
    case PUSH:
    {
        double d[] = { 0, 0, (mCur[1] - mOld[1])*pCurr.d };

        rotate_vq( d, pCurr.r );

        pCurr.c[0] += d[0];
        pCurr.c[1] += d[1];
        pCurr.c[2] += d[2];
        break;
    }
    case PAN:
    {
        double d[] = { 
            -0.3 * pCurr.d * (mCur[0] - mOld[0]),
            -0.3 * pCurr.d * (mCur[1] - mOld[1]),
            0
        };

        rotate_vq( d, pCurr.r );

        pCurr.c[0] += d[0];
        pCurr.c[1] += d[1];
        pCurr.c[2] += d[2];
        break;
    }
    default:
        break;
    }

    update();

    mOld[0] = mCur[0];
    mOld[1] = mCur[1];
  }
    
  void end()
  {
      m_mode = Mode.NONE;
  }

  void home( double ex, double ey, double ez,
             double cx, double cy, double cz,
             double ux, double uy, double uz )
  {
    double look[] = new double[3];
    double side[] = new double[3];
    double up[]   = new double[3];

      look[0] = cx - ex;
      look[1] = cy - ey;
      look[2] = cz - ez;

      up[0] = ux;
      up[1] = uy;
      up[2] = uz;

      pCurr.c[0] = cx;
      pCurr.c[1] = cy;
      pCurr.c[2] = cz;

      pCurr.d = Math.sqrt( look[0]*look[0] + look[1]*look[1] + look[2]*look[2] );

      if( pCurr.d != 0.0 )
      {
          look[0] /= pCurr.d;
          look[1] /= pCurr.d;
          look[2] /= pCurr.d;
      }

      side[0] = look[1]*up[2] - look[2]*up[1];
      side[1] = look[2]*up[0] - look[0]*up[2];
      side[2] = look[0]*up[1] - look[1]*up[0];

      double ls = Math.sqrt( side[0]*side[0] + side[1]*side[1] + side[2]*side[2] );

      side[0] /= ls;
      side[1] /= ls;
      side[2] /= ls;

      double lu = Math.sqrt( up[0]*up[0] + up[1]*up[1] + up[2]*up[2] );

      up[0] = side[1]*look[2] - side[2]*look[1];
      up[1] = side[2]*look[0] - side[0]*look[2];
      up[2] = side[0]*look[1] - side[1]*look[0];

      double m[][] = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };

      m[0][0] = side[0]; m[1][0] = side[1]; m[2][0] = side[2];
      m[0][1] = up[0];   m[1][1] = up[1];   m[2][1] = up[2];
      m[0][2] = -look[0]; m[1][2] = -look[1]; m[2][2] = -look[2];

      double tq[] = {
        1 + m[0][0] + m[1][1] - look[2],
        1 + m[0][0] - m[1][1] + look[2],
        1 - m[0][0] + m[1][1] + look[2],
        1 - m[0][0] - m[1][1] - look[2],
      };

      int j = 0;

      for( int i=1; i<4; ++i )
          if( tq[i] > tq[j] )
              j = i;

      double s = 0.5*Math.sqrt( 1.0/tq[j] );

      if( j == 0 )
      {
          pCurr.r[0] = s*(m[1][2]-m[2][1]);
          pCurr.r[1] = s*(m[2][0]-m[0][2]);
          pCurr.r[2] = s*(m[0][1]-m[1][0]);
          pCurr.r[3] = s*tq[0];
      }
      else if( j == 1 )
      {
          pCurr.r[0] = s*tq[1];
          pCurr.r[1] = s*(m[0][1]+m[1][0]);
          pCurr.r[2] = s*(m[2][0]+m[0][2]);
          pCurr.r[3] = s*(m[1][2]-m[2][1]);
      }
      else if( j == 2 )
      {
          pCurr.r[0] = s*(m[0][1]+m[1][0]);
          pCurr.r[1] = s*tq[2];
          pCurr.r[2] = s*(m[1][2]+m[2][1]);
          pCurr.r[3] = s*(m[2][0]-m[0][2]);
      }
      else 
      {
          pCurr.r[0] = s*(m[2][0]+m[0][2]);
          pCurr.r[1] = s*(m[1][2]+m[2][1]);
          pCurr.r[2] = s*tq[3];
          pCurr.r[3] = s*(m[0][1]-m[1][0]);
      }

      pHome = new Position( pCurr );
      update();
    }
    
    void home()
    {
        pCurr = new Position( pHome );
        update();
    }

    void apply_trackball( double[] m0, double[] m1 )
    {
        final double tbsize = 0.8;
        final double r2 = tbsize*tbsize / 2.0;

        double d1 = m0[0]*m0[0] + m0[1]*m0[1];
        double d2 = m1[0]*m1[0] + m1[1]*m1[1];

        double sp1[] = { m0[0], m0[1], d1 < tbsize ? Math.sqrt( 2.0*tbsize-d1 ) : tbsize/Math.sqrt(d1) };
        double sp2[] = { m1[0], m1[1], d2 < tbsize ? Math.sqrt( 2.0*tbsize-d2 ) : tbsize/Math.sqrt(d2) };

        double axis[] = {
          sp2[1]*sp1[2] - sp2[2]*sp1[1],
          sp2[2]*sp1[0] - sp2[0]*sp1[2],
          sp2[0]*sp1[1] - sp2[1]*sp1[0] 
        };

        double saxis[] = { axis[0], axis[1], axis[2] };

        normalize( axis );
        rotate_vq( axis, pCurr.r );

        sp2[0] -= sp1[0];
        sp2[1] -= sp1[1];
        sp2[2] -= sp1[2];

        double angle = Math.sqrt( sp2[0]*sp2[0] + sp2[1]*sp2[1] + sp2[2]*sp2[2] ) / tbsize;

        if( angle >  1.0 )  angle =  1.0;
        if( angle < -1.0 )  angle = -1.0;

        angle = Math.asin( angle );

        final double ch = Math.cos( 0.5*angle );
        final double sh = Math.sin( 0.5*angle );

        double qr[] = { axis[0]*sh, axis[1]*sh, axis[2]*sh, ch };
        rotate_qq( pCurr.r, qr );
    }

    void rotate_vq( double[] v, final double[] q )
    {
        double xx = 2.0 * q[0] * q[0];
        double xy = 2.0 * q[0] * q[1];
        double xz = 2.0 * q[0] * q[2];
        double yy = 2.0 * q[1] * q[1];
        double yz = 2.0 * q[1] * q[2];
        double zz = 2.0 * q[2] * q[2];
        double wx = 2.0 * q[3] * q[0];
        double wy = 2.0 * q[3] * q[1];
        double wz = 2.0 * q[3] * q[2];

        double c[] = { v[0], v[1], v[2] };

        v[0] = (1.0-yy-zz)*c[0] + (xy-wz)*c[1] + (xz+wy)*c[2];
        v[1] = (xy+wz)*c[0] + (1.0-xx-zz)*c[1] + (yz-wx)*c[2];
        v[2] = (xz-wy)*c[0] + (yz+wx)*c[1] + (1.0-xx-yy)*c[2];
    }
    
    void rotate_qq( double[] q, final double[] r )
    {
        double x = r[3]*q[0] + r[0]*q[3] + r[1]*q[2] - r[2]*q[1];
        double y = r[3]*q[1] - r[0]*q[2] + r[1]*q[3] + r[2]*q[0];
        double z = r[3]*q[2] + r[0]*q[1] - r[1]*q[0] + r[2]*q[3];
        double w = r[3]*q[3] - r[0]*q[0] - r[1]*q[1] - r[2]*q[2];
    
        q[0] = x;
        q[1] = y;
        q[2] = z;
        q[3] = w;
    }

    void normalize( double[] v )
    {
        double l = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];

        if( l != 0 )
        {
            l = Math.sqrt( l );

            v[0] /= l;
            v[1] /= l;
            v[2] /= l;
        }
    }

  void update()
  {
    mvMatrix[0 ] = 1.0 - 2.0*pCurr.r[1]*pCurr.r[1] - 2.0*pCurr.r[2]*pCurr.r[2];
    mvMatrix[1 ] = 2.0*pCurr.r[0]*pCurr.r[1]-2.0*pCurr.r[2]*pCurr.r[3];
    mvMatrix[2 ] = 2.0*pCurr.r[0]*pCurr.r[2]+2.0*pCurr.r[1]*pCurr.r[3];
    mvMatrix[3 ] = 0;

    mvMatrix[4 ] = 2.0*pCurr.r[0]*pCurr.r[1]+2.0*pCurr.r[2]*pCurr.r[3];
    mvMatrix[5 ] = 1.0 - 2.0*pCurr.r[0]*pCurr.r[0] - 2.0*pCurr.r[2]*pCurr.r[2];
    mvMatrix[6 ] = 2.0*pCurr.r[1]*pCurr.r[2]-2.0*pCurr.r[0]*pCurr.r[3];
    mvMatrix[7 ] = 0;

    mvMatrix[8 ] = 2.0*pCurr.r[0]*pCurr.r[2]-2.0*pCurr.r[1]*pCurr.r[3];
    mvMatrix[9 ] = 2.0*pCurr.r[1]*pCurr.r[2]+2.0*pCurr.r[0]*pCurr.r[3];
    mvMatrix[10] = 1.0 - 2.0*pCurr.r[0]*pCurr.r[0]- 2.0*pCurr.r[1]*pCurr.r[1];
    mvMatrix[11] = 0;

    mvMatrix[12] = -(mvMatrix[0]*pCurr.c[0] + mvMatrix[4]*pCurr.c[1] + mvMatrix[8]*pCurr.c[2]);
    mvMatrix[13] = -(mvMatrix[1]*pCurr.c[0] + mvMatrix[5]*pCurr.c[1] + mvMatrix[9]*pCurr.c[2]);
    mvMatrix[14] = -(mvMatrix[2]*pCurr.c[0] + mvMatrix[6]*pCurr.c[1] + mvMatrix[10]*pCurr.c[2] + pCurr.d);
    mvMatrix[15] = 1;
      
      // set default projection matrix
    double zFar = 100;
    double zNear = 0.1;
  
    double f = 2.414213562;
    double aspect = (double)parent.width / (double)parent.height;
          
    prMatrix[0] = f/aspect;
    prMatrix[1] = 0;
    prMatrix[2] = 0;
    prMatrix[3] = 0;
    prMatrix[4] = 0; 
    prMatrix[5] = f;
    prMatrix[6] = 0;
    prMatrix[7] = 0;
    prMatrix[8] = 0;
    prMatrix[9] = 0;
    prMatrix[10] = (zFar+zNear)/(zNear-zFar);
    prMatrix[11] = -1;
    prMatrix[12] = 0;
    prMatrix[13] = 0;
    prMatrix[14] = 2*zFar*zNear/(zNear-zFar);
    prMatrix[15] = 0;
  }

  public void mouseEvent( MouseEvent e )
  {    
    float maxSide = parent.max( parent.width, parent.height );
    
    float mx = e.getX() / maxSide - 1.0f;
    float my = 1.0f - e.getY() / maxSide;
    
    if( e.getAction() == MouseEvent.PRESS )
    {
      Mode mode = Mode.NONE;
      
      if( e.getButton() == PApplet.LEFT )
        mode = Mode.ROTATE;
      else if( e.getButton() == PApplet.RIGHT )
        mode = Mode.ZOOM;
      else if( e.getButton() == PApplet.CENTER )
        mode = Mode.PAN;
        
      begin( mode, mx, my );
      parent.redraw();
    }
    else if( e.getAction() == MouseEvent.DRAG )
    {
      move( mx, my );
      parent.redraw();
    }
    else if( e.getAction() == MouseEvent.RELEASE )
      end();
  }

  public void keyEvent( KeyEvent e ) 
  {
    if( e.getAction() == KeyEvent.PRESS ) 
    {
      if( e.getKey() == ' ' )
      {
        home();
        parent.redraw();
      }
    }
  }
}

