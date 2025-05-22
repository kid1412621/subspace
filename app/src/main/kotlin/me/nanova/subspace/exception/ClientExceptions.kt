package me.nanova.subspace.exception

open class ClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class AuthenticationException(
    message: String = "Authentication failed. Please check credentials.",
    cause: Throwable? = null
) : ClientException(message, cause)

class NetworkException(
    message: String = "Network error. Could not connect to the server.",
    cause: Throwable? = null
) : ClientException(message, cause)

class OperationFailedException(
    message: String = "The operation failed.",
    cause: Throwable? = null
) : ClientException(message, cause)

class ClientNotSupportedException(
    message: String = "This client type or version is not supported.",
    cause: Throwable? = null
) : ClientException(message, cause)

class RateLimitException(
    message: String = "Too many requests. Please try again later.",
    cause: Throwable? = null
) : ClientException(message, cause)

class ResourceNotFoundException(
    message: String = "The requested resource was not found.",
    cause: Throwable? = null
) : ClientException(message, cause)
