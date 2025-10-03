import { useEffect } from "react";
import { useUser } from "@clerk/clerk-react";
import { useNavigate } from "react-router-dom";

const DashboardRedirect = () => {
  const { user, isLoaded } = useUser();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isLoaded) return;

    const role = user?.publicMetadata.role;

    if (role === "ADMIN") {
      navigate("/admindashboard", { replace: true });
    } else if (role === "USER") {
      navigate("/userdashboard", { replace: true });
    } else {
      navigate("/", { replace: true }); // fallback to landing
    }
  }, [isLoaded, user, navigate]);

  return <p>Redirecting to your dashboard...</p>;
};

export default DashboardRedirect;
