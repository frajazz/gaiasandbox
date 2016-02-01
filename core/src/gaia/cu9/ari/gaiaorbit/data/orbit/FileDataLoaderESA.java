package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;

public class FileDataLoaderESA {
    int count = 0;

    public FileDataLoaderESA() {
        super();
    }

    /**
     * Loads the data in the input stream into an OrbitData object.
     * @param data
     * @param referenceFrame
     * @throws Exception
     */
    public OrbitData load(InputStream data) throws Exception {
        OrbitData orbitData = new OrbitData();

        BufferedReader br = new BufferedReader(new InputStreamReader(data));
        String line;
        boolean metaFlag = false;
        int dNum = 20, ind = 0;
        
        Timestamp last = new Timestamp( 0 );
        while( ( line = br.readLine() ) != null ) 
        {
            if( line.isEmpty() )
                continue;
            
            if( line.startsWith( "META_START" ) )
            {
                metaFlag = true;
                continue;
            }
            
            if( line.startsWith( "META_STOP" ) )
            {
                metaFlag = false;
                continue;
            }
            
            if( !metaFlag ) 
            {
                // Read line
                String[] tokens = line.split( "\\s+" );
                if( tokens.length >= 7 && !tokens[ 0 ].isEmpty() ) 
                {
                    if( ++ind > dNum )
                    {
                        ind = 0;
                        // Valid data line
                        Timestamp t = Timestamp.valueOf( tokens[ 0 ].replace( 'T', ' ' ) );
                        Matrix4d transform = new Matrix4d();
                        transform.scl( Constants.KM_TO_U );
                        if( !t.equals( last ) ) 
                        {
                            orbitData.time.add( t );
                            Vector3d pos = new Vector3d( parsed( tokens[ 1 ] ), parsed( tokens[ 2 ] ), parsed( tokens[ 3 ] ) );
                            pos.mul( transform );
                            orbitData.x.add( pos.y );
                            orbitData.y.add( pos.z );
                            orbitData.z.add( pos.x );
                            last.setTime( t.getTime() );
                        }
                    }
                }
            }
        }

        br.close();

        return orbitData;
    }

    protected float parsef(String str) {
        return Float.valueOf(str);
    }

    protected double parsed( String str ) 
    {
        return Double.valueOf( str.replace( "D", "E" ) );
    }

    protected int parsei(String str) {
        return Integer.valueOf(str);
    }

}
