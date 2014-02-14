package com.lubin.rpc.server.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Values {

	private final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	private HashMap<String, Object> values;

	public Values() {
		values = new HashMap<String, Object>(8);
	}

	public Values(int size) {
		values = new HashMap<String, Object>(size, 1.0f);
	}

	public Values(Values from) {
		values = new HashMap<String, Object>(from.values);
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Values)) {
			return false;
		}
		return values.equals(((Values) object).values);
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	public void put(String key, String value) {
		values.put(key, value);
	}

	public void putAll(Values other) {
		values.putAll(other.values);
	}

	public void put(String key, Byte value) {
		values.put(key, value);
	}

	public void put(String key, Short value) {
		values.put(key, value);
	}

	public void put(String key, Integer value) {
		values.put(key, value);
	}

	public void put(String key, Long value) {
		values.put(key, value);
	}

	public void put(String key, Float value) {
		values.put(key, value);
	}

	public void put(String key, Double value) {
		values.put(key, value);
	}

	public void put(String key, Boolean value) {
		values.put(key, value);
	}

	public void put(String key, byte[] value) {
		values.put(key, value);
	}

	public void putNull(String key) {
		values.put(key, null);
	}

	public int size() {
		return values.size();
	}

	public void remove(String key) {
		values.remove(key);
	}

	public void clear() {
		values.clear();
	}

	public boolean containsKey(String key) {
		return values.containsKey(key);
	}

	public Object get(String key) {
		return values.get(key);
	}

	public String getAsString(String key) {
		Object value = values.get(key);
		return value != null ? value.toString() : null;
	}

	public Long getAsLong(String key) {
		Object value = values.get(key);
		try {
			return value != null ? ((Number) value).longValue() : null;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				try {
					return Long.valueOf(value.toString());
				} catch (NumberFormatException e2) {
					logger.error("Cannot parse Long value for " + value
							+ " at key " + key);
					return null;
				}
			} else {
				logger.error("Cannot cast value for " + key + " to a Long: "
						+ value, e);
				return null;
			}
		}
	}

	public Integer getAsInteger(String key) {
		Object value = values.get(key);
		try {
			return value != null ? ((Number) value).intValue() : null;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				try {
					return Integer.valueOf(value.toString());
				} catch (NumberFormatException e2) {
					logger.error("Cannot parse Integer value for " + value
							+ " at key " + key);
					return null;
				}
			} else {
				logger.error("Cannot cast value for " + key + " to a Integer: "
						+ value, e);
				return null;
			}
		}
	}

	public Short getAsShort(String key) {
		Object value = values.get(key);
		try {
			return value != null ? ((Number) value).shortValue() : null;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				try {
					return Short.valueOf(value.toString());
				} catch (NumberFormatException e2) {
					logger.error("Cannot parse Short value for " + value
							+ " at key " + key);
					return null;
				}
			} else {
				logger.error("Cannot cast value for " + key + " to a Short: "
						+ value, e);
				return null;
			}
		}
	}

	public Byte getAsByte(String key) {
		Object value = values.get(key);
		try {
			return value != null ? ((Number) value).byteValue() : null;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				try {
					return Byte.valueOf(value.toString());
				} catch (NumberFormatException e2) {
					logger.error("Cannot parse Byte value for " + value
							+ " at key " + key);
					return null;
				}
			} else {
				logger.error("Cannot cast value for " + key + " to a Byte: "
						+ value, e);
				return null;
			}
		}
	}

	public Double getAsDouble(String key) {
		Object value = values.get(key);
		try {
			return value != null ? ((Number) value).doubleValue() : null;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				try {
					return Double.valueOf(value.toString());
				} catch (NumberFormatException e2) {
					logger.error("Cannot parse Double value for " + value
							+ " at key " + key);
					return null;
				}
			} else {
				logger.error("Cannot cast value for " + key + " to a Double: "
						+ value, e);
				return null;
			}
		}
	}

	public Float getAsFloat(String key) {
		Object value = values.get(key);
		try {
			return value != null ? ((Number) value).floatValue() : null;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				try {
					return Float.valueOf(value.toString());
				} catch (NumberFormatException e2) {
					logger.error("Cannot parse Float value for " + value
							+ " at key " + key);
					return null;
				}
			} else {
				logger.error("Cannot cast value for " + key + " to a Float: "
						+ value, e);
				return null;
			}
		}
	}

	public Boolean getAsBoolean(String key) {
		Object value = values.get(key);
		try {
			return (Boolean) value;
		} catch (ClassCastException e) {
			if (value instanceof CharSequence) {
				return Boolean.valueOf(value.toString());
			} else if (value instanceof Number) {
				return ((Number) value).intValue() != 0;
			} else {
				logger.error("Cannot cast value for " + key + " to a Boolean: "
						+ value, e);
				return null;
			}
		}
	}

	public byte[] getAsByteArray(String key) {
		Object value = values.get(key);
		if (value instanceof byte[]) {
			return (byte[]) value;
		} else {
			return null;
		}
	}

	public Set<Map.Entry<String, Object>> valueSet() {
		return values.entrySet();
	}

	public Set<String> keySet() {
		return values.keySet();
	}

	/**
	 * Unsupported, here until we get proper bulk insert APIs. {@hide}
	 */
	@Deprecated
	public void putStringList(String key, List<String> value) {
		values.put(key, value);
	}

	/**
	 * Unsupported, here until we get proper bulk insert APIs. {@hide}
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public List<String> getStringList(String key) {
		return (List<String>) values.get(key);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String name : values.keySet()) {
			String value = getAsString(name);
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(name + "=" + value);
		}
		return sb.toString();
	}
}