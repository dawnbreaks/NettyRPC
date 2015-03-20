package com.lubin.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.lang.reflect.InvocationHandler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigDecimalSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.BigIntegerSerializer;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyListSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptyMapSerializer;
import de.javakaffee.kryoserializers.CollectionsEmptySetSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonListSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonMapSerializer;
import de.javakaffee.kryoserializers.CollectionsSingletonSetSerializer;
import de.javakaffee.kryoserializers.CopyForIterateCollectionSerializer;
import de.javakaffee.kryoserializers.CopyForIterateMapSerializer;
import de.javakaffee.kryoserializers.DateSerializer;
import de.javakaffee.kryoserializers.EnumMapSerializer;
import de.javakaffee.kryoserializers.EnumSetSerializer;
import de.javakaffee.kryoserializers.GregorianCalendarSerializer;
import de.javakaffee.kryoserializers.JdkProxySerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
//import org.xerial.snappy.Snappy;

/**
 * general serializer.
 */

public final class KryoSerializer {
    private static final ThreadLocal<Kryo> threadLocalkryoCodec = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo _kryo = new KryoReflectionFactorySupport() {

                @Override
                @SuppressWarnings( { "rawtypes", "unchecked" } )
                public Serializer<?> getDefaultSerializer( final Class type ) {
                    if ( EnumSet.class.isAssignableFrom( type ) ) {
                        return new EnumSetSerializer();
                    }
                    if ( EnumMap.class.isAssignableFrom( type ) ) {
                        return new EnumMapSerializer();
                    }
                    if ( Collection.class.isAssignableFrom( type ) ) {
                        return new CopyForIterateCollectionSerializer();
                    }
                    if ( Map.class.isAssignableFrom( type ) ) {
                        return new CopyForIterateMapSerializer();
                    }
                    if ( Date.class.isAssignableFrom( type ) ) {
                        return new DateSerializer( type );
                    }
                    return super.getDefaultSerializer( type );
                }
            };
            _kryo.setRegistrationRequired(false);
            _kryo.register( Arrays.asList( "" ).getClass(), new ArraysAsListSerializer() );
            _kryo.register( Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer() );
            _kryo.register( Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer() );
            _kryo.register( Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer() );
            _kryo.register( Collections.singletonList( "" ).getClass(), new CollectionsSingletonListSerializer() );
            _kryo.register( Collections.singleton( "" ).getClass(), new CollectionsSingletonSetSerializer() );
            _kryo.register( Collections.singletonMap( "", "" ).getClass(), new CollectionsSingletonMapSerializer() );
            _kryo.register( BigDecimal.class, new BigDecimalSerializer() );
            _kryo.register( BigInteger.class, new BigIntegerSerializer() );
            //      _kryo.register( Pattern.class, new RegexSerializer() );
            //      _kryo.register( BitSet.class, new BitSetSerializer() );
            //      _kryo.register( URI.class, new URISerializer() );
            //      _kryo.register( UUID.class, new UUIDSerializer() );
            _kryo.register( GregorianCalendar.class, new GregorianCalendarSerializer() );
            _kryo.register( InvocationHandler.class, new JdkProxySerializer() );
            UnmodifiableCollectionsSerializer.registerSerializers( _kryo );
            SynchronizedCollectionsSerializer.registerSerializers( _kryo );
            //      DeflateSerializer d;      
            //      _kryo..setDefaultSerializer(CompatibleFieldSerializer.class);
            return _kryo;
        }
    };

    private KryoSerializer() {
    }

    public static byte[] write(Object obj) {
        return write(obj, -1);
    }


    /*
     * with zip 
     */
    //  public static byte[] write(Object obj, int maxBufferSize) {
    //    Kryo kryo = _threadLocalKryo.get();
    //    Output output = new Output(1024*10, maxBufferSize);
    //    Deflater deflater = new Deflater(4, true);
    //    DeflaterOutputStream deflaterStream = new DeflaterOutputStream(output, deflater);
    //    Output deflaterOutput = new Output(deflaterStream);
    //    kryo.writeClassAndObject(deflaterOutput, obj);
    //    deflaterOutput.flush();
    //    try {
    //    	deflaterStream.finish();
    //    } catch (IOException ex) {
    //            throw new KryoException(ex);
    //    }
    //    return output.toBytes();
    //  }
    //
    //  public static Object read(byte[] bytes) {
    //	if(bytes==null)
    //		return null;
    //    Kryo kryo = _threadLocalKryo.get();
    //    Input input = new Input(bytes);
    //    Inflater inflater = new Inflater(true);
    //    InflaterInputStream inflaterInput = new InflaterInputStream(input, inflater);
    //    return kryo.readClassAndObject(new Input(inflaterInput));
    //  }
    //  


    //   //with snappy-java
    //  	public static byte[] write(Object obj, int maxBufferSize)
    //  	{
    //	    Kryo kryo = _threadLocalKryo.get();
    //	    Output output = new Output(1024*10, maxBufferSize);
    //	    kryo.writeClassAndObject(output, obj);
    //	    try
    //	    {
    //			return Snappy.compress(output.toBytes());
    //		} catch (IOException e) 
    //		{
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //			return null;
    //		}
    //	  }
    //
    // 	public static Object read(byte[] bytes)
    // 	{		
    // 		try 
    // 		{
    // 	 		if(bytes==null)
    // 				return null;
    // 	 		Kryo kryo = _threadLocalKryo.get();
    // 			Input input = new Input(Snappy.uncompress(bytes));
    // 			return kryo.readClassAndObject(input);
    // 		} catch (IOException e) 
    // 		{
    // 			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //			return null;
    // 		}
    //	  
    // 	}



    public static byte[] write(Object obj, int maxBufferSize) {
        Kryo kryo = threadLocalkryoCodec.get();
        Output output = new Output(1024*4, maxBufferSize);
        kryo.writeClassAndObject(output, obj);
        return output.toBytes();
    }

    public static Object read(byte[] bytes) {
        if(bytes==null)
            return null;
        Kryo kryo = threadLocalkryoCodec.get();
        Input input = new Input(bytes);
        return kryo.readClassAndObject(input);
    }
    
    //performance improvements, avoid unnecessary memory copy.
    //NettyRPC encoder write directly to ByteBuf, and do not need to encode to an byte[] array and then write byte[] to ByteBuf
    public static void write(Object obj, ByteBuf buf) {
        Kryo kryo = threadLocalkryoCodec.get();
        ByteBufOutputStream byteOutputStream = new ByteBufOutputStream(buf);
        Output output = new Output(1024*4, -1);
        output.setOutputStream(byteOutputStream);
        kryo.writeClassAndObject(output, output);
        output.flush();
    }

    //performance improvements, avoid unnecessary memory copy.
    //NettyRPC decoder read directly from ByteBuf, and do not need to copy bytebuf to byte[] array.
    public static Object read(ByteBuf buf) {
        if(buf==null)
            return null;
        Input input = new Input(new ByteBufInputStream(buf));
        Kryo kryo = threadLocalkryoCodec.get();
        return kryo.readClassAndObject(input);
    }
}


