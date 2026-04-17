import { createContext, useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import api from "../api/axiosConfig";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On app startup, check if they have a valid token
  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        // Extract username and roles from your Spring Boot JWT
        setUser({
          username: decodedToken.sub,
          roles: decodedToken.roles,
        });
      } catch (error) {
        console.error("Invalid token on startup");
      }
    }
    setLoading(false);
  }, []);

  // Login function: Saves tokens and sets user state
  const login = (accessToken, refreshToken) => {
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    const decodedToken = jwtDecode(accessToken);
    setUser({ username: decodedToken.sub, roles: decodedToken.roles });
  };

  // THE LOGOUT FEATURE
  const logout = async () => {
    try {
      // Call the backend to invalidate the session (optional but best practice)
      await api.post("/api/auth/logout");
    } catch (error) {
      console.error("Backend logout error", error);
    } finally {
      // ALWAYS destroy local tokens, regardless of what the backend says
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      setUser(null);

      // Redirect to login page
      window.location.href = "/login";
    }
  };

  if (loading) return <div>Loading FlashTix...</div>;

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
